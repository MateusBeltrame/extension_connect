package br.com.mateushb.extensionconnectapp.model

import java.time.Instant

// Conteúdo da aba Arquivos (spec 006): fotos e documentos do projeto, em qualquer status. Os cards
// mostram só os enviados; "Envios em andamento" mostra os pendentes de quem está logado
data class ArquivosProjeto(
    val fotos: List<FotoDetalhes> = emptyList(),
    val documentos: List<ArquivoDetalhes> = emptyList()
) {
    // Calculados uma vez por emissão (não a cada acesso da tela). Do mais recente para o mais antigo,
    // comparando como Instant (o texto ISO muda de tamanho com a fração de segundo); empate ou data
    // inválida cai no id
    val fotosEnviadas: List<FotoDetalhes> = fotos
        .filter { it.foto.statusEnvio == StatusEnvio.ENVIADO }
        .sortedWith(compareByDescending<FotoDetalhes> { instante(it.foto.dataEnvio) }.thenByDescending { it.foto.id })

    val documentosEnviados: List<ArquivoDetalhes> = documentos
        .filter { it.arquivo.statusEnvio == StatusEnvio.ENVIADO }
        .sortedWith(compareByDescending<ArquivoDetalhes> { instante(it.arquivo.dataEnvio) }.thenByDescending { it.arquivo.id })

    // Todos os pendentes, na ordem da fila (o mais antigo primeiro)
    private val pendentes: List<Pendente> = (
        fotos.map { it.foto }.filter { it.statusEnvio != StatusEnvio.ENVIADO }.map { foto ->
            Pendente(
                foto.autorId,
                EnvioPendente(
                    chave = EnvioEmAndamento.chaveFoto(foto.id),
                    id = foto.id,
                    nomeArquivo = foto.nomeArquivo,
                    dataEnvio = foto.dataEnvio,
                    statusEnvio = foto.statusEnvio,
                    progresso = foto.progresso,
                    foto = foto
                )
            )
        } + documentos.map { it.arquivo }.filter { it.statusEnvio != StatusEnvio.ENVIADO }.map { arquivo ->
            Pendente(
                arquivo.autorId,
                EnvioPendente(
                    chave = EnvioEmAndamento.chaveDocumento(arquivo.id),
                    id = arquivo.id,
                    nomeArquivo = arquivo.nomeArquivo,
                    dataEnvio = arquivo.dataEnvio,
                    statusEnvio = arquivo.statusEnvio,
                    progresso = arquivo.progresso,
                    formato = arquivo.extensao
                )
            )
        }
        ).sortedWith(compareBy<Pendente> { instante(it.envio.dataEnvio) }.thenBy { it.envio.id })

    // A fila é do aparelho de quem envia: cada usuário vê só os próprios
    fun enviosPendentes(usuarioId: Int): List<EnvioPendente> =
        pendentes.filter { it.autorId == usuarioId }.map { it.envio }

    private data class Pendente(val autorId: Int, val envio: EnvioPendente)

    private fun instante(iso: String): Instant = lerInstanteIso(iso) ?: Instant.MIN
}

// Uma linha de "Envios em andamento": foto (com miniatura) ou documento (com o selo do formato)
data class EnvioPendente(
    val chave: String,
    val id: Int,
    val nomeArquivo: String,
    val dataEnvio: String,
    val statusEnvio: StatusEnvio,
    val progresso: Int?,
    val foto: Foto? = null,
    val formato: FormatoDocumento? = null
)
