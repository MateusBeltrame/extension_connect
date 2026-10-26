package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.envio.FilaEnvio
import br.com.mateushb.extensionconnectapp.data.envio.MonitorConectividade
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.local.ArmazenamentoArquivos
import br.com.mateushb.extensionconnectapp.data.local.ArquivoRecusado
import br.com.mateushb.extensionconnectapp.data.repository.ArquivoRepository
import br.com.mateushb.extensionconnectapp.data.repository.ProjetoRepository
import br.com.mateushb.extensionconnectapp.data.repository.RegistroDiarioRepository
import br.com.mateushb.extensionconnectapp.model.Arquivo
import br.com.mateushb.extensionconnectapp.model.ArquivosProjeto
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes
import br.com.mateushb.extensionconnectapp.model.RascunhoAplicacao
import br.com.mateushb.extensionconnectapp.model.RegistroDiario
import br.com.mateushb.extensionconnectapp.model.ResultadoEnvioArquivo
import br.com.mateushb.extensionconnectapp.model.ResultadoMudancaEstagio
import br.com.mateushb.extensionconnectapp.model.ResultadoPublicarRegistro
import br.com.mateushb.extensionconnectapp.model.TransicaoEstagio
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.AcaoProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.DetalhesProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.EnvioArquivoUiState
import br.com.mateushb.extensionconnectapp.ui.state.ExclusaoArquivoUiState
import br.com.mateushb.extensionconnectapp.ui.state.ExclusaoRegistroDiarioUiState
import br.com.mateushb.extensionconnectapp.ui.state.ListaArquivosUiState
import br.com.mateushb.extensionconnectapp.ui.state.ListaRegistrosDiarioUiState
import br.com.mateushb.extensionconnectapp.ui.state.MudarEstagioUiState
import br.com.mateushb.extensionconnectapp.ui.state.PreparacaoFotoUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarRegistroDiarioUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// Escopo: a entrada ProjetoDetalhes da pilha. Separado do ProjetoViewModel (lista e filtros) para a
// tela de detalhes não montar estado de lista nem ler as preferências de filtro
class ProjetoDetalhesViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ProjetoRepository = AppDatabase.obterInstancia(application).let {
        ProjetoRepository(it.projetoDao(), it.usuarioDao())
    },
    private val diarioRepository: RegistroDiarioRepository = RegistroDiarioRepository(
        AppDatabase.obterInstancia(application).registroDiarioDao()
    ),
    private val arquivoRepository: ArquivoRepository = AppDatabase.obterInstancia(application).let {
        ArquivoRepository(
            it.arquivoDao(),
            it.fotoDao(),
            ArmazenamentoArquivos(application),
            MonitorConectividade.obterInstancia(application),
            FilaEnvio.obterInstancia(application)
        )
    }
) : AndroidViewModel(application) {

    // Estados da tela de detalhes
    var detalhesUiState: DetalhesProjetoUiState by mutableStateOf(DetalhesProjetoUiState.Carregando)
        private set

    var mudarEstagioUiState: MudarEstagioUiState by mutableStateOf(MudarEstagioUiState.Ocioso)
        private set

    var acaoUiState: AcaoProjetoUiState by mutableStateOf(AcaoProjetoUiState.Ocioso)
        private set

    // Estados da aba Diário (spec 005). Separados dos detalhes: falhar o diário não esconde a Visão geral
    var listaRegistrosUiState: ListaRegistrosDiarioUiState by mutableStateOf(ListaRegistrosDiarioUiState.Carregando)
        private set

    var salvarRegistroUiState: SalvarRegistroDiarioUiState by mutableStateOf(SalvarRegistroDiarioUiState.Ocioso)
        private set

    var exclusaoRegistroUiState: ExclusaoRegistroDiarioUiState by mutableStateOf(ExclusaoRegistroDiarioUiState.Ocioso)
        private set

    // Estados da aba Arquivos (spec 006). Também separados: falhar a aba não esconde as outras
    var listaArquivosUiState: ListaArquivosUiState by mutableStateOf(ListaArquivosUiState.Carregando)
        private set

    var preparacaoFotoUiState: PreparacaoFotoUiState by mutableStateOf(PreparacaoFotoUiState.Ocioso)
        private set

    // Documento e foto têm estados de envio próprios: copiar um documento grande não trava o sheet da
    // foto, e o erro de um não aparece no fluxo do outro
    var envioDocumentoUiState: EnvioArquivoUiState by mutableStateOf(EnvioArquivoUiState.Ocioso)
        private set

    var envioFotoUiState: EnvioArquivoUiState by mutableStateOf(EnvioArquivoUiState.Ocioso)
        private set

    var exclusaoArquivoUiState: ExclusaoArquivoUiState by mutableStateOf(ExclusaoArquivoUiState.Ocioso)
        private set

    private var detalhesJob: Job? = null
    private var projetoIdObservado: Int? = null

    private var diarioJob: Job? = null
    private var projetoIdDiario: Int? = null

    private var arquivosJob: Job? = null
    private var projetoIdArquivos: Int? = null
    private var preparacaoFotoJob: Job? = null

    // Foto ou documento escolhido antes de os detalhes carregarem (ex.: o sistema encerrou o app com a
    // câmera aberta e a Activity foi recriada). Roda assim que os detalhes chegam, em vez de se perder
    private sealed interface PedidoArquivo {
        val usuario: Usuario

        data class Documento(override val usuario: Usuario, val uri: Uri) : PedidoArquivo
        data class Foto(override val usuario: Usuario, val uri: Uri, val daCamera: Boolean) : PedidoArquivo
    }

    private var pedidoPendente: PedidoArquivo? = null

    // Toque duplo em "Confirmar ciência": a permissão lida do estado só muda quando o Room reemite
    private var confirmandoCiencia = false

    // Última leitura do banco: as ações conferem permissão sobre ela, não sobre o que a tela mostrava
    private val detalhesAtuais: ProjetoDetalhes?
        get() = (detalhesUiState as? DetalhesProjetoUiState.Sucesso)?.detalhes

    private val arquivosAtuais: ArquivosProjeto?
        get() = (listaArquivosUiState as? ListaArquivosUiState.Sucesso)?.arquivos

    // ===== Operações da tela de detalhes =====

    // Também é o "Tentar novamente". Na rotação o LaunchedEffect chama de novo: se o mesmo projeto já
    // está sendo observado, não volta para Carregando (isso tiraria o sheet aberto da composição e
    // descartaria o que foi digitado)
    fun carregarDetalhesProjeto(projetoId: Int) {
        if (projetoId == projetoIdObservado && detalhesJob?.isActive == true &&
            detalhesUiState !is DetalhesProjetoUiState.Erro
        ) {
            return
        }
        projetoIdObservado = projetoId
        detalhesJob?.cancel()
        detalhesUiState = DetalhesProjetoUiState.Carregando
        detalhesJob = viewModelScope.launch {
            repository.projetoDetalhesEmTempoReal(projetoId)
                .catch { detalhesUiState = DetalhesProjetoUiState.Erro("Erro ao carregar projeto. Tente novamente.") }
                .collect { detalhes ->
                    detalhesUiState = if (detalhes != null) {
                        DetalhesProjetoUiState.Sucesso(detalhes)
                    } else {
                        DetalhesProjetoUiState.NaoEncontrado("Projeto não encontrado no banco local.")
                    }
                    atenderPedidoPendente()
                }
        }
    }

    // ===== Ciclo de estágio (spec 004) =====

    // Ignora toque duplo (já salvando) e transição que o usuário não pode mais fazer (estado reativo)
    fun mudarEstagio(
        usuario: Usuario,
        transicao: TransicaoEstagio,
        aplicacao: RascunhoAplicacao?,
        justificativa: String?
    ) {
        val detalhes = detalhesAtuais ?: return
        if (mudarEstagioUiState is MudarEstagioUiState.Salvando) return
        if (transicao !in detalhes.transicoesPara(usuario)) {
            mudarEstagioUiState = MudarEstagioUiState.EstagioMudou(MENSAGEM_ESTAGIO_MUDOU)
            return
        }

        mudarEstagioUiState = MudarEstagioUiState.Salvando
        viewModelScope.launch {
            mudarEstagioUiState = try {
                when (repository.mudarEstagio(detalhes.projeto, transicao, usuario, aplicacao, justificativa)) {
                    ResultadoMudancaEstagio.Sucesso -> MudarEstagioUiState.Sucesso(transicao.mensagemSucesso)
                    ResultadoMudancaEstagio.EstagioMudou -> MudarEstagioUiState.EstagioMudou(MENSAGEM_ESTAGIO_MUDOU)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                MudarEstagioUiState.Erro("Erro ao mudar estágio. Tente novamente.")
            }
        }
    }

    fun confirmarCiencia(usuario: Usuario) {
        val detalhes = detalhesAtuais ?: return
        if (confirmandoCiencia || !detalhes.podeConfirmarCiencia(usuario)) return

        confirmandoCiencia = true
        viewModelScope.launch {
            acaoUiState = try {
                repository.confirmarCiencia(detalhes.projeto.id)
                AcaoProjetoUiState.Sucesso("Ciência confirmada")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                AcaoProjetoUiState.Erro("Erro ao confirmar ciência. Tente novamente.")
            } finally {
                confirmandoCiencia = false
            }
        }
    }

    // Sem atualização otimista: o checkbox reflete o banco, então uma falha não muda a tela. A inversão
    // é feita no banco (NOT concluida), para dois toques rápidos alternarem duas vezes
    fun alternarEtapa(usuario: Usuario, etapa: Etapa) {
        val detalhes = detalhesAtuais ?: return
        if (!detalhes.podeMarcarEtapa(usuario)) return
        if (detalhes.resumo.etapas.none { it.id == etapa.id }) return

        viewModelScope.launch {
            try {
                repository.alternarConclusaoEtapa(etapa.id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                acaoUiState = AcaoProjetoUiState.Erro("Erro ao atualizar etapa. Tente novamente.")
            }
        }
    }

    // ===== Diário (spec 005) =====

    // Também é o "Tentar novamente" da aba. Como nos detalhes, a rotação não reinicia a observação
    fun carregarDiario(projetoId: Int) {
        if (projetoId == projetoIdDiario && diarioJob?.isActive == true &&
            listaRegistrosUiState !is ListaRegistrosDiarioUiState.Erro
        ) {
            return
        }
        projetoIdDiario = projetoId
        diarioJob?.cancel()
        listaRegistrosUiState = ListaRegistrosDiarioUiState.Carregando
        diarioJob = viewModelScope.launch {
            diarioRepository.registrosEmTempoReal(projetoId)
                .catch { listaRegistrosUiState = ListaRegistrosDiarioUiState.Erro("Erro ao carregar o diário. Tente novamente.") }
                .collect { listaRegistrosUiState = ListaRegistrosDiarioUiState.Sucesso(it) }
        }
    }

    // O tipo vem do papel na última leitura do banco; o DAO confere de novo dentro da transação.
    // Ignora toque duplo (já salvando) e texto inválido (a tela valida antes)
    fun publicarRegistro(usuario: Usuario, texto: String, etapaId: Int?) {
        val detalhes = detalhesAtuais ?: return
        if (salvarRegistroUiState is SalvarRegistroDiarioUiState.Salvando) return
        if (RegistroDiario.erroTexto(texto) != null) return
        val tipo = detalhes.tipoRegistroPara(usuario)
        if (tipo == null) {
            salvarRegistroUiState = SalvarRegistroDiarioUiState.SemPermissao(MENSAGEM_SEM_PERMISSAO_DIARIO)
            return
        }

        salvarRegistroUiState = SalvarRegistroDiarioUiState.Salvando
        viewModelScope.launch {
            salvarRegistroUiState = try {
                when (diarioRepository.publicar(detalhes.projeto.id, usuario.id, tipo, texto, etapaId)) {
                    ResultadoPublicarRegistro.Sucesso -> SalvarRegistroDiarioUiState.Sucesso("Registro publicado")
                    ResultadoPublicarRegistro.SemPermissao ->
                        SalvarRegistroDiarioUiState.SemPermissao(MENSAGEM_SEM_PERMISSAO_DIARIO)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                SalvarRegistroDiarioUiState.Erro("Erro ao publicar registro. Tente novamente.")
            }
        }
    }

    // Só o próprio registro, em projeto não finalizado; o DELETE repete as duas condições
    fun excluirRegistro(usuario: Usuario, registroId: Int) {
        val detalhes = detalhesAtuais ?: return
        if (exclusaoRegistroUiState is ExclusaoRegistroDiarioUiState.Excluindo) return
        val registros = (listaRegistrosUiState as? ListaRegistrosDiarioUiState.Sucesso)?.registros ?: return
        val registro = registros.firstOrNull { it.registro.id == registroId }?.registro ?: return
        if (!detalhes.podeExcluirRegistro(usuario, registro)) {
            exclusaoRegistroUiState = ExclusaoRegistroDiarioUiState.Erro(MENSAGEM_NAO_PODE_EXCLUIR)
            return
        }

        exclusaoRegistroUiState = ExclusaoRegistroDiarioUiState.Excluindo
        viewModelScope.launch {
            exclusaoRegistroUiState = try {
                // Nada apagado: projeto finalizado, conta desativada ou registro já excluído em outro
                // aparelho. Tentar de novo não resolve, então não é o "Tente novamente"
                if (diarioRepository.excluir(registro.id, usuario.id)) {
                    ExclusaoRegistroDiarioUiState.Sucesso("Registro excluído com sucesso")
                } else {
                    ExclusaoRegistroDiarioUiState.Erro(MENSAGEM_NAO_PODE_EXCLUIR)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                ExclusaoRegistroDiarioUiState.Erro(MENSAGEM_ERRO_EXCLUIR_REGISTRO)
            }
        }
    }

    // ===== Arquivos e fotos (spec 006) =====

    // Também é o "Tentar novamente" da aba. Como nos detalhes, a rotação não reinicia a observação
    fun carregarArquivos(projetoId: Int) {
        if (projetoId == projetoIdArquivos && arquivosJob?.isActive == true &&
            listaArquivosUiState !is ListaArquivosUiState.Erro
        ) {
            return
        }
        projetoIdArquivos = projetoId
        arquivosJob?.cancel()
        listaArquivosUiState = ListaArquivosUiState.Carregando
        arquivosJob = viewModelScope.launch {
            arquivoRepository.arquivosEmTempoReal(projetoId)
                .catch { listaArquivosUiState = ListaArquivosUiState.Erro("Erro ao carregar os arquivos. Tente novamente.") }
                .collect { listaArquivosUiState = ListaArquivosUiState.Sucesso(it) }
        }
    }

    // Documento vai direto para a fila (sem sheet de autorização). O DAO confere a permissão de novo
    fun enviarDocumento(usuario: Usuario, uri: Uri) {
        val detalhes = detalhesAtuais
        if (detalhes == null) {
            guardarPedido(PedidoArquivo.Documento(usuario, uri))
            return
        }
        if (envioDocumentoUiState is EnvioArquivoUiState.Enviando) return
        if (!detalhes.podeEnviarArquivos(usuario)) {
            envioDocumentoUiState = EnvioArquivoUiState.SemPermissao(MENSAGEM_SEM_PERMISSAO_ARQUIVOS)
            return
        }

        envioDocumentoUiState = EnvioArquivoUiState.Enviando
        viewModelScope.launch {
            envioDocumentoUiState = try {
                when (arquivoRepository.enviarDocumento(detalhes.projeto.id, usuario.id, uri)) {
                    ResultadoEnvioArquivo.Sucesso -> EnvioArquivoUiState.Sucesso("Documento adicionado à fila de envio")
                    ResultadoEnvioArquivo.SemPermissao -> EnvioArquivoUiState.SemPermissao(MENSAGEM_SEM_PERMISSAO_ARQUIVOS)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ArquivoRecusado) {
                EnvioArquivoUiState.Erro(e.message.orEmpty())
            } catch (_: Exception) {
                EnvioArquivoUiState.Erro(MENSAGEM_ERRO_ENVIAR)
            }
        }
    }

    // Comprime a foto tirada ou escolhida e abre o sheet de autorização (Pronta). Uma foto preparada
    // antes e não enviada é descartada
    fun prepararFoto(usuario: Usuario, uri: Uri, daCamera: Boolean) {
        val detalhes = detalhesAtuais
        if (detalhes == null) {
            guardarPedido(PedidoArquivo.Foto(usuario, uri, daCamera))
            return
        }
        // Uma foto já indo para a fila termina antes de outra ser preparada
        if (envioFotoUiState is EnvioArquivoUiState.Enviando) return
        if (!detalhes.podeEnviarArquivos(usuario)) {
            envioFotoUiState = EnvioArquivoUiState.SemPermissao(MENSAGEM_SEM_PERMISSAO_ARQUIVOS)
            return
        }
        descartarFoto()

        preparacaoFotoUiState = PreparacaoFotoUiState.Preparando
        preparacaoFotoJob = viewModelScope.launch {
            preparacaoFotoUiState = try {
                PreparacaoFotoUiState.Pronta(arquivoRepository.prepararFoto(uri, daCamera))
            } catch (e: CancellationException) {
                throw e
            } catch (e: ArquivoRecusado) {
                PreparacaoFotoUiState.Erro(e.message.orEmpty())
            } catch (_: Exception) {
                PreparacaoFotoUiState.Erro("Não foi possível usar esta foto. Use JPG, PNG ou HEIC.")
            }
        }
    }

    // "Enviar foto" do sheet. Em erro de gravação o sheet continua com a foto, para tentar de novo
    fun enviarFoto(usuario: Usuario, autorizada: Boolean) {
        val detalhes = detalhesAtuais ?: return
        val foto = (preparacaoFotoUiState as? PreparacaoFotoUiState.Pronta)?.foto ?: return
        if (envioFotoUiState is EnvioArquivoUiState.Enviando) return
        if (!detalhes.podeEnviarArquivos(usuario)) {
            descartarFoto()
            envioFotoUiState = EnvioArquivoUiState.SemPermissao(MENSAGEM_SEM_PERMISSAO_ARQUIVOS)
            return
        }

        envioFotoUiState = EnvioArquivoUiState.Enviando
        viewModelScope.launch {
            envioFotoUiState = try {
                val resultado = arquivoRepository.enviarFoto(detalhes.projeto.id, usuario.id, foto, autorizada)
                // O Repository já apagou o temporário nos dois casos
                preparacaoFotoUiState = PreparacaoFotoUiState.Ocioso
                when (resultado) {
                    ResultadoEnvioArquivo.Sucesso -> EnvioArquivoUiState.Sucesso("Foto adicionada à fila de envio")
                    ResultadoEnvioArquivo.SemPermissao -> EnvioArquivoUiState.SemPermissao(MENSAGEM_SEM_PERMISSAO_ARQUIVOS)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                EnvioArquivoUiState.Erro(MENSAGEM_ERRO_ENVIAR)
            }
        }
    }

    // Cancelar, tocar fora ou arrastar o sheet de autorização (também cancela uma compressão em andamento)
    fun descartarFoto() {
        if (envioFotoUiState is EnvioArquivoUiState.Enviando) return
        preparacaoFotoJob?.cancel()
        val foto = (preparacaoFotoUiState as? PreparacaoFotoUiState.Pronta)?.foto
        preparacaoFotoUiState = PreparacaoFotoUiState.Ocioso
        foto?.let { arquivoRepository.descartarFoto(it) }
    }

    // Sem atualização otimista: o Switch reflete o banco. O UPDATE repete autor e projeto não finalizado
    fun alterarAutorizacaoMural(usuario: Usuario, fotoId: Int, autorizada: Boolean) {
        val detalhes = detalhesAtuais ?: return
        val foto = arquivosAtuais?.fotos?.firstOrNull { it.foto.id == fotoId }?.foto ?: return
        if (!detalhes.podeGerenciarArquivo(usuario, foto.autorId)) {
            acaoUiState = AcaoProjetoUiState.Erro(MENSAGEM_FOTO_NAO_PODE_MUDAR)
            return
        }

        viewModelScope.launch {
            try {
                if (!arquivoRepository.alterarAutorizacao(fotoId, usuario.id, autorizada)) {
                    acaoUiState = AcaoProjetoUiState.Erro(MENSAGEM_FOTO_NAO_PODE_MUDAR)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                acaoUiState = AcaoProjetoUiState.Erro("Erro ao atualizar a foto. Tente novamente.")
            }
        }
    }

    fun excluirDocumento(usuario: Usuario, arquivoId: Int) {
        val autorId = arquivosAtuais?.documentos?.firstOrNull { it.arquivo.id == arquivoId }?.arquivo?.autorId ?: return
        excluirArquivo(
            usuario = usuario,
            autorId = autorId,
            mensagemSucesso = "Documento excluído com sucesso",
            mensagemErro = "Erro ao excluir documento. Tente novamente.",
            mensagemNaoPode = "Este documento não pode mais ser excluído."
        ) { arquivoRepository.excluirDocumento(arquivoId, usuario.id) }
    }

    fun excluirFoto(usuario: Usuario, fotoId: Int) {
        val autorId = arquivosAtuais?.fotos?.firstOrNull { it.foto.id == fotoId }?.foto?.autorId ?: return
        excluirArquivo(
            usuario = usuario,
            autorId = autorId,
            mensagemSucesso = "Foto excluída com sucesso",
            mensagemErro = "Erro ao excluir foto. Tente novamente.",
            mensagemNaoPode = "Esta foto não pode mais ser excluída."
        ) { arquivoRepository.excluirFoto(fotoId, usuario.id) }
    }

    // Mesmo fluxo para documento e foto: confere na última leitura, depois o DELETE repete as condições
    private fun excluirArquivo(
        usuario: Usuario,
        autorId: Int,
        mensagemSucesso: String,
        mensagemErro: String,
        mensagemNaoPode: String,
        excluir: suspend () -> Boolean
    ) {
        val detalhes = detalhesAtuais ?: return
        if (exclusaoArquivoUiState is ExclusaoArquivoUiState.Excluindo) return
        if (!detalhes.podeGerenciarArquivo(usuario, autorId)) {
            exclusaoArquivoUiState = ExclusaoArquivoUiState.Erro(mensagemNaoPode)
            return
        }

        exclusaoArquivoUiState = ExclusaoArquivoUiState.Excluindo
        viewModelScope.launch {
            exclusaoArquivoUiState = try {
                if (excluir()) ExclusaoArquivoUiState.Sucesso(mensagemSucesso) else ExclusaoArquivoUiState.Erro(mensagemNaoPode)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                ExclusaoArquivoUiState.Erro(mensagemErro)
            }
        }
    }

    // Uris para os apps do sistema (câmera e abrir documento); null = não foi possível / arquivo ausente
    fun novaUriCaptura(): Uri? =
        try {
            arquivoRepository.novaUriCaptura()
        } catch (_: Exception) {
            null
        }

    fun uriDoDocumento(arquivo: Arquivo): Uri? =
        try {
            arquivoRepository.uriDoDocumento(arquivo)
        } catch (_: Exception) {
            null
        }

    fun limparEnvioDocumentoUiState() {
        envioDocumentoUiState = EnvioArquivoUiState.Ocioso
    }

    fun limparEnvioFotoUiState() {
        envioFotoUiState = EnvioArquivoUiState.Ocioso
    }

    // Detalhes ainda carregando: o pedido espera (fica só o último). Se a carga já falhou, avisa em vez
    // de descartar calado
    private fun guardarPedido(pedido: PedidoArquivo) {
        if (detalhesUiState is DetalhesProjetoUiState.Carregando) {
            pedidoPendente = pedido
            return
        }
        when (pedido) {
            is PedidoArquivo.Documento -> envioDocumentoUiState = EnvioArquivoUiState.Erro(MENSAGEM_PROJETO_NAO_CARREGOU)
            is PedidoArquivo.Foto -> preparacaoFotoUiState = PreparacaoFotoUiState.Erro(MENSAGEM_PROJETO_NAO_CARREGOU)
        }
    }

    private fun atenderPedidoPendente() {
        val pedido = pedidoPendente ?: return
        pedidoPendente = null
        when (pedido) {
            is PedidoArquivo.Documento -> enviarDocumento(pedido.usuario, pedido.uri)
            is PedidoArquivo.Foto -> prepararFoto(pedido.usuario, pedido.uri, pedido.daCamera)
        }
    }

    fun limparExclusaoArquivoUiState() {
        exclusaoArquivoUiState = ExclusaoArquivoUiState.Ocioso
    }

    // Só o Erro é consumido; Pronta continua até enviar ou descartar
    fun limparErroPreparacaoFoto() {
        if (preparacaoFotoUiState is PreparacaoFotoUiState.Erro) preparacaoFotoUiState = PreparacaoFotoUiState.Ocioso
    }

    // A foto comprimida que não foi enviada não fica no cache
    override fun onCleared() {
        (preparacaoFotoUiState as? PreparacaoFotoUiState.Pronta)?.let { arquivoRepository.descartarFoto(it.foto) }
        super.onCleared()
    }

    fun limparSalvarRegistroUiState() {
        salvarRegistroUiState = SalvarRegistroDiarioUiState.Ocioso
    }

    fun limparExclusaoRegistroUiState() {
        exclusaoRegistroUiState = ExclusaoRegistroDiarioUiState.Ocioso
    }

    fun limparMudarEstagioUiState() {
        mudarEstagioUiState = MudarEstagioUiState.Ocioso
    }

    fun limparAcaoUiState() {
        acaoUiState = AcaoProjetoUiState.Ocioso
    }

    private companion object {
        const val MENSAGEM_ESTAGIO_MUDOU = "O estágio do projeto mudou. Confira e tente de novo."
        const val MENSAGEM_SEM_PERMISSAO_DIARIO = "Você não pode mais publicar neste projeto."
        const val MENSAGEM_ERRO_EXCLUIR_REGISTRO = "Erro ao excluir registro. Tente novamente."
        const val MENSAGEM_NAO_PODE_EXCLUIR = "Este registro não pode mais ser excluído."
        const val MENSAGEM_SEM_PERMISSAO_ARQUIVOS = "Você não pode mais enviar arquivos neste projeto."
        const val MENSAGEM_ERRO_ENVIAR = "Erro ao enviar arquivo. Tente novamente."
        const val MENSAGEM_PROJETO_NAO_CARREGOU = "Não foi possível enviar: o projeto não carregou. Tente novamente."
        const val MENSAGEM_FOTO_NAO_PODE_MUDAR = "Esta foto não pode mais ser alterada."
    }
}
