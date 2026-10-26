package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.repository.ProjetoRepository
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.RascunhoEtapa
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.model.ResultadoSalvarProjeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.FormularioProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarProjetoUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

// Escopo: a entrada NovoProjeto da pilha. O rascunho dos 3 passos fica aqui, e não em remember na tela,
// porque é compartilhado entre os passos e precisa sobreviver à rotação (exceção ao Artigo 5.4
// justificada no plan.md da spec 003)
class NovoProjetoViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ProjetoRepository = AppDatabase.obterInstancia(application).let {
        ProjetoRepository(it.projetoDao(), it.usuarioDao())
    }
) : AndroidViewModel(application) {

    // Estados do wizard
    var formularioUiState: FormularioProjetoUiState by mutableStateOf(FormularioProjetoUiState.Carregando)
        private set

    var salvarUiState: SalvarProjetoUiState by mutableStateOf(SalvarProjetoUiState.Ocioso)
        private set

    var rascunho: RascunhoProjeto by mutableStateOf(RascunhoProjeto())
        private set

    // 1 Projeto · 2 Cronograma · 3 Participantes
    var passoAtual: Int by mutableIntStateOf(1)
        private set

    // Professor logado entra como orientador; fechar sem mudar nada além dele não pede confirmação
    var orientadorInicial: Usuario? by mutableStateOf(null)
        private set

    private var dadosJob: Job? = null
    private var iniciado = false

    // Depois de gravar, a tela ainda aparece durante a transição para os detalhes: um segundo toque
    // em "Salvar projeto" não pode tentar gravar de novo
    private var projetoSalvo = false
    private var proximaChaveEtapa = 1L

    // ===== Abertura do wizard =====

    // Uma vez por instância: a recomposição da rota (ou a rotação) não apaga o rascunho
    fun iniciar(usuario: Usuario) {
        if (iniciado) return
        iniciado = true
        if (usuario.perfil == PerfilUsuario.PROFESSOR) {
            val professor = usuario.copy(senhaHash = "")
            orientadorInicial = professor
            rascunho = rascunho.copy(orientador = professor)
        }
        carregarDados()
    }

    // Também é o "Tentar novamente"
    fun carregarDados() {
        dadosJob?.cancel()
        formularioUiState = FormularioProjetoUiState.Carregando
        dadosJob = viewModelScope.launch {
            combine(
                repository.professoresAtivosEmTempoReal,
                repository.candidatosParticipantesEmTempoReal
            ) { professores, candidatos ->
                FormularioProjetoUiState.Sucesso(professores = professores, candidatos = candidatos) as FormularioProjetoUiState
            }
                // Mapeamento e ordenação (Collator) das listas fora da thread principal
                .flowOn(Dispatchers.Default)
                .catch { emit(FormularioProjetoUiState.Erro("Erro ao carregar dados do banco local. Tente novamente.")) }
                .collect { formularioUiState = it }
        }
    }

    // ===== Passos =====

    fun alterarRascunho(transformacao: (RascunhoProjeto) -> RascunhoProjeto) {
        rascunho = transformacao(rascunho)
    }

    // A tela valida o passo antes de chamar
    fun avancarPasso() {
        if (passoAtual < 3) passoAtual++
    }

    fun voltarPasso() {
        if (passoAtual > 1) passoAtual--
    }

    // ===== Cronograma (Passo 2) =====

    fun adicionarEtapa(nome: String = "") {
        val etapa = RascunhoEtapa(chave = proximaChaveEtapa++, nome = nome)
        rascunho = rascunho.copy(etapas = rascunho.etapas + etapa)
    }

    fun removerEtapa(chave: Long) {
        rascunho = rascunho.copy(etapas = rascunho.etapas.filterNot { it.chave == chave })
    }

    fun moverEtapa(de: Int, para: Int) {
        val etapas = rascunho.etapas
        if (de !in etapas.indices || para !in etapas.indices || de == para) return
        rascunho = rascunho.copy(etapas = etapas.toMutableList().apply { add(para, removeAt(de)) })
    }

    // ===== Participantes (Passo 3) =====

    fun adicionarParticipante(aluno: Usuario) {
        if (rascunho.limiteAtingido || rascunho.participantes.any { it.id == aluno.id }) return
        rascunho = rascunho.copy(participantes = rascunho.participantes + aluno)
    }

    fun removerParticipante(alunoId: Int) {
        rascunho = rascunho.copy(participantes = rascunho.participantes.filterNot { it.id == alunoId })
    }

    // ===== Salvar =====

    fun salvar(usuario: Usuario) {
        if (projetoSalvo || salvarUiState is SalvarProjetoUiState.Salvando) return
        salvarUiState = SalvarProjetoUiState.Salvando
        viewModelScope.launch {
            salvarUiState = try {
                when (val resultado = repository.salvarNovoProjeto(rascunho, autorId = usuario.id)) {
                    is ResultadoSalvarProjeto.Sucesso -> {
                        // Sem isso, os participantes recém-gravados apareceriam como "ocupados" na saída
                        projetoSalvo = true
                        dadosJob?.cancel()
                        SalvarProjetoUiState.Sucesso(resultado.projetoId)
                    }

                    is ResultadoSalvarProjeto.AlunoIndisponivel ->
                        SalvarProjetoUiState.Erro(
                            "${resultado.aluno.nome} já participa de Grupo ${resultado.nomeGrupo}. Remova para continuar."
                        )

                    is ResultadoSalvarProjeto.AlunoInativo ->
                        SalvarProjetoUiState.Erro("${resultado.aluno.nome} não está mais ativo. Remova para continuar.")

                    ResultadoSalvarProjeto.OrientadorInativo -> {
                        rascunho = rascunho.copy(orientador = null)
                        passoAtual = 1
                        SalvarProjetoUiState.Erro("O orientador escolhido não está mais ativo. Escolha outro.")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                SalvarProjetoUiState.Erro("Erro ao salvar projeto no banco local. Tente novamente.")
            }
        }
    }

    fun limparSalvarUiState() {
        salvarUiState = SalvarProjetoUiState.Ocioso
    }
}
