package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.repository.ProjetoRepository
import br.com.mateushb.extensionconnectapp.data.repository.RegistroDiarioRepository
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes
import br.com.mateushb.extensionconnectapp.model.RascunhoAplicacao
import br.com.mateushb.extensionconnectapp.model.RegistroDiario
import br.com.mateushb.extensionconnectapp.model.ResultadoMudancaEstagio
import br.com.mateushb.extensionconnectapp.model.ResultadoPublicarRegistro
import br.com.mateushb.extensionconnectapp.model.TransicaoEstagio
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.AcaoProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.DetalhesProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.ExclusaoRegistroDiarioUiState
import br.com.mateushb.extensionconnectapp.ui.state.ListaRegistrosDiarioUiState
import br.com.mateushb.extensionconnectapp.ui.state.MudarEstagioUiState
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
    )
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

    private var detalhesJob: Job? = null
    private var projetoIdObservado: Int? = null

    private var diarioJob: Job? = null
    private var projetoIdDiario: Int? = null

    // Toque duplo em "Confirmar ciência": a permissão lida do estado só muda quando o Room reemite
    private var confirmandoCiencia = false

    // Última leitura do banco: as ações conferem permissão sobre ela, não sobre o que a tela mostrava
    private val detalhesAtuais: ProjetoDetalhes?
        get() = (detalhesUiState as? DetalhesProjetoUiState.Sucesso)?.detalhes

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
    }
}
