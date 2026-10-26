package br.com.mateushb.extensionconnectapp.data.repository

import android.net.Uri
import br.com.mateushb.extensionconnectapp.data.envio.FilaEnvio
import br.com.mateushb.extensionconnectapp.data.envio.MonitorConectividade
import br.com.mateushb.extensionconnectapp.data.local.ArmazenamentoArquivos
import br.com.mateushb.extensionconnectapp.data.local.dao.ArquivoDao
import br.com.mateushb.extensionconnectapp.data.local.dao.FotoDao
import br.com.mateushb.extensionconnectapp.data.local.entity.ArquivoEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.FotoEntity
import br.com.mateushb.extensionconnectapp.model.Arquivo
import br.com.mateushb.extensionconnectapp.model.ArquivosProjeto
import br.com.mateushb.extensionconnectapp.model.EnvioEmAndamento
import br.com.mateushb.extensionconnectapp.model.Foto
import br.com.mateushb.extensionconnectapp.model.FotoPreparada
import br.com.mateushb.extensionconnectapp.model.ResultadoEnvioArquivo
import br.com.mateushb.extensionconnectapp.model.StatusEnvio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.Instant

// Aba Arquivos (spec 006): documentos e fotos andam juntos na tela e na fila de envio, por isso um
// único Repository para as duas entidades. A cópia local fica no ArmazenamentoArquivos; a FilaEnvio
// pega os itens gravados aqui (sempre como pendentes) e os leva até ENVIADO.
// Arquivo recusado (formato, tamanho, foto ilegível) chega ao ViewModel como ArquivoRecusado
class ArquivoRepository(
    private val arquivoDao: ArquivoDao,
    private val fotoDao: FotoDao,
    private val armazenamento: ArmazenamentoArquivos,
    private val conectividade: MonitorConectividade,
    private val fila: FilaEnvio
) {

    // O banco diz só pendente ou enviado; o status exibido dos pendentes vem da rede e da fila (em
    // memória). O progresso da fila reemite só este combine, sem reconsultar o banco
    fun arquivosEmTempoReal(projetoId: Int): Flow<ArquivosProjeto> =
        combine(
            fotoDao.observarFotosPorProjeto(projetoId),
            arquivoDao.observarArquivosPorProjeto(projetoId),
            conectividade.conectado,
            fila.emAndamento
        ) { fotos, documentos, conectado, emAndamento ->
            ArquivosProjeto(
                fotos = fotos.map { linha ->
                    val detalhes = linha.toFotoDetalhes()
                    val (status, progresso) = statusExibido(
                        detalhes.foto.statusEnvio, EnvioEmAndamento.chaveFoto(detalhes.foto.id), conectado, emAndamento
                    )
                    detalhes.copy(foto = detalhes.foto.copy(statusEnvio = status, progresso = progresso))
                },
                documentos = documentos.map { linha ->
                    val detalhes = linha.toArquivoDetalhes()
                    val (status, progresso) = statusExibido(
                        detalhes.arquivo.statusEnvio, EnvioEmAndamento.chaveDocumento(detalhes.arquivo.id), conectado, emAndamento
                    )
                    detalhes.copy(arquivo = detalhes.arquivo.copy(statusEnvio = status, progresso = progresso))
                }
            )
        }

    // ===== Envio =====

    // Copia, valida e põe na fila. Sem permissão (conferida no banco, na transação), a cópia é apagada
    suspend fun enviarDocumento(projetoId: Int, autorId: Int, uri: Uri): ResultadoEnvioArquivo {
        val copia = withContext(Dispatchers.IO) { armazenamento.copiarDocumento(uri) }
        val arquivo = Arquivo(
            projetoId = projetoId,
            autorId = autorId,
            nomeArquivo = copia.nomeArquivo,
            extensao = copia.formato,
            tamanhoBytes = copia.tamanhoBytes,
            caminhoLocal = copia.caminho,
            dataEnvio = Instant.now().toString(),
            statusEnvio = StatusEnvio.AGUARDANDO_ENVIO
        )
        val id = try {
            arquivoDao.enfileirar(ArquivoEntity.fromArquivo(arquivo))
        } catch (e: Exception) {
            apagar(copia.caminho)
            throw e
        }
        if (id == null) {
            apagar(copia.caminho)
            return ResultadoEnvioArquivo.SemPermissao
        }
        return ResultadoEnvioArquivo.Sucesso
    }

    suspend fun prepararFoto(uri: Uri, daCamera: Boolean): FotoPreparada =
        withContext(Dispatchers.IO) { armazenamento.prepararFoto(uri, daCamera) }

    // Só apaga um temporário pequeno: pode rodar fora de corrotina (onCleared do ViewModel)
    fun descartarFoto(foto: FotoPreparada) = armazenamento.apagar(foto.caminhoTemporario)

    suspend fun enviarFoto(projetoId: Int, autorId: Int, preparada: FotoPreparada, autorizada: Boolean): ResultadoEnvioArquivo {
        val caminho = withContext(Dispatchers.IO) { armazenamento.guardarFoto(preparada) }
        val foto = Foto(
            projetoId = projetoId,
            autorId = autorId,
            nomeArquivo = preparada.nomeArquivo,
            tamanhoBytes = preparada.tamanhoBytes,
            caminhoLocal = caminho,
            dataEnvio = Instant.now().toString(),
            autorizadaMural = autorizada,
            statusEnvio = StatusEnvio.AGUARDANDO_ENVIO
        )
        val id = try {
            fotoDao.enfileirar(FotoEntity.fromFoto(foto))
        } catch (e: Exception) {
            apagar(caminho)
            throw e
        }
        // Gravou ou não pode mais enviar: em nenhum dos casos o temporário volta a ser usado
        apagar(preparada.caminhoTemporario)
        if (id == null) {
            apagar(caminho)
            return ResultadoEnvioArquivo.SemPermissao
        }
        return ResultadoEnvioArquivo.Sucesso
    }

    // ===== Gerenciar (só o autor, projeto não finalizado: conferido no SQL) =====

    // false = nada mudou (foto de outra pessoa, já excluída, projeto finalizado ou conta desativada)
    suspend fun alterarAutorizacao(fotoId: Int, autorId: Int, autorizada: Boolean): Boolean =
        fotoDao.alterarAutorizacaoDoAutor(fotoId, autorId, autorizada) > 0

    // O arquivo só é apagado depois do DELETE, que é onde a permissão é conferida
    suspend fun excluirDocumento(arquivoId: Int, autorId: Int): Boolean {
        val caminho = arquivoDao.buscarCaminho(arquivoId)
        val excluiu = arquivoDao.excluirDoAutor(arquivoId, autorId) > 0
        if (excluiu && caminho != null) apagar(caminho)
        return excluiu
    }

    suspend fun excluirFoto(fotoId: Int, autorId: Int): Boolean {
        val caminho = fotoDao.buscarCaminho(fotoId)
        val excluiu = fotoDao.excluirDoAutor(fotoId, autorId) > 0
        if (excluiu && caminho != null) apagar(caminho)
        return excluiu
    }

    // ===== Uris para a câmera e para abrir documento =====

    fun novaUriCaptura(): Uri = armazenamento.novaUriCaptura()

    fun uriDoDocumento(arquivo: Arquivo): Uri? = armazenamento.uriCompartilhavel(arquivo.caminhoLocal)

    // Pendente sem internet: "Aguardando envio · sem internet". Com internet: "Enviando", com o progresso
    // da fila se for o item da vez, senão 0% (esperando a vez, spec 006 HU-09)
    private fun statusExibido(
        statusBanco: StatusEnvio,
        chave: String,
        conectado: Boolean,
        emAndamento: EnvioEmAndamento?
    ): Pair<StatusEnvio, Int?> = when {
        statusBanco == StatusEnvio.ENVIADO -> StatusEnvio.ENVIADO to null
        !conectado -> StatusEnvio.AGUARDANDO_ENVIO to null
        emAndamento?.chave == chave -> StatusEnvio.ENVIANDO to emAndamento.progresso
        else -> StatusEnvio.ENVIANDO to 0
    }

    private suspend fun apagar(caminho: String) = withContext(Dispatchers.IO) { armazenamento.apagar(caminho) }
}
