package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.local.preferences.AppPreferencesManager
import br.com.mateushb.extensionconnectapp.data.repository.ProjetoRepository
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.FiltroProjetos
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.ListaProjetosUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.Collator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

// Escopo: a entrada ProjetosLista da pilha (criado no composable da rota, não no AppNavHost). "Sair da
// conta" e a troca de item da BottomBar limpam a pilha, então busca, lista e observação nunca passam
// de um usuário para o outro
class ProjetoViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ProjetoRepository = AppDatabase.obterInstancia(application).let {
        ProjetoRepository(it.projetoDao(), it.usuarioDao())
    },
    private val preferencesManager: AppPreferencesManager = AppPreferencesManager(application)
) : AndroidViewModel(application) {

    // Estados da tela de listagem (abas Lista e Quadro)
    var listaUiState: ListaProjetosUiState by mutableStateOf(ListaProjetosUiState.Carregando)
        private set

    // Busca não é lembrada entre execuções; filtros sim (spec 002, HU-03/HU-05)
    var busca: String by mutableStateOf("")
        private set

    var filtroAtual: FiltroProjetos by mutableStateOf(preferencesManager.obterFiltroProjetos())
        private set

    private val buscaFlow = MutableStateFlow(busca)
    private val filtroFlow = MutableStateFlow(filtroAtual)

    private var projetosJob: Job? = null
    private var usuarioCarregado: Usuario? = null

    private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

    // ===== Operações da tela de listagem =====

    // Também é o "Tentar novamente". Não reinicia se o mesmo usuário já está sendo observado sem erro
    // (recomposição da rota)
    fun carregarProjetos(usuario: Usuario) {
        val mesmoUsuario = usuarioCarregado == usuario && projetosJob?.isActive == true
        if (mesmoUsuario && listaUiState !is ListaProjetosUiState.Erro) return
        usuarioCarregado = usuario
        observarProjetos(usuario)
    }

    fun alterarBusca(texto: String) {
        busca = texto
        buscaFlow.value = texto
    }

    fun aplicarFiltro(filtro: FiltroProjetos) {
        filtroAtual = filtro
        filtroFlow.value = filtro
        preferencesManager.salvarFiltroProjetos(filtro)
    }

    // Chips do quadro: atalhos para os mesmos filtros do bottom sheet
    fun alternarSouOrientador() {
        aplicarFiltro(filtroAtual.copy(souOrientador = !filtroAtual.souOrientador))
    }

    fun alternarSoParados() {
        aplicarFiltro(filtroAtual.copy(soParados = !filtroAtual.soParados))
    }

    fun limparFiltros() {
        alterarBusca("")
        aplicarFiltro(FiltroProjetos())
    }

    private fun observarProjetos(usuario: Usuario) {
        projetosJob?.cancel()
        listaUiState = ListaProjetosUiState.Carregando
        projetosJob = viewModelScope.launch {
            combine(
                repository.projetosVisiveisEmTempoReal(usuario),
                buscaFlow,
                filtroFlow,
                diaAtual()
            ) { projetos, texto, filtro, hoje ->
                val orientadores = projetos.map { it.orientador }
                    .distinctBy { it.id }
                    .sortedWith(compareBy(collator) { it.nome })
                val filtroValido = filtroValidoPara(filtro, usuario, orientadores)
                val termo = ProjetoResumo.normalizar(texto)
                filtroValido to ListaProjetosUiState.Sucesso(
                    projetos = ordenarProjetos(
                        projetos.filter { it.correspondeABusca(termo) && atendeFiltro(it, filtroValido, usuario, hoje) }
                    ),
                    existemProjetos = projetos.isNotEmpty(),
                    orientadores = orientadores,
                    hoje = hoje
                ) as ListaProjetosUiState
            }
                // Mapeamento, normalização e ordenação fora da thread principal
                .flowOn(Dispatchers.Default)
                .catch {
                    emit(filtroFlow.value to ListaProjetosUiState.Erro("Erro ao carregar projetos do banco local. Tente novamente."))
                }
                .collect { (filtroValido, estado) ->
                    // Filtro salvo que não vale para este usuário é descartado, para não ficar ativo e invisível
                    if (filtroValido != filtroAtual) aplicarFiltro(filtroValido)
                    listaUiState = estado
                }
        }
    }

    // Filtro salvo no aparelho pode ter vindo de outro login: "Sou orientador" só existe para Professor, e
    // o orientador escolhido precisa estar entre os orientadores que este usuário enxerga
    private fun filtroValidoPara(filtro: FiltroProjetos, usuario: Usuario, orientadores: List<Usuario>): FiltroProjetos =
        filtro.copy(
            orientadorId = filtro.orientadorId?.takeIf { id -> orientadores.any { it.id == id } },
            souOrientador = filtro.souOrientador && usuario.perfil == PerfilUsuario.PROFESSOR
        )

    private fun atendeFiltro(
        resumo: ProjetoResumo,
        filtro: FiltroProjetos,
        usuario: Usuario,
        hoje: LocalDate
    ): Boolean {
        val projeto = resumo.projeto
        return (filtro.estagio == null || projeto.estagio == filtro.estagio) &&
            (filtro.tema == null || projeto.tema == filtro.tema) &&
            (filtro.orientadorId == null || projeto.orientadorId == filtro.orientadorId) &&
            (!filtro.souOrientador || projeto.orientadorId == usuario.id) &&
            (!filtro.soParados || resumo.parado(hoje))
    }

    // Não finalizados primeiro; dentro de cada grupo, por título (spec 002, HU-01)
    private fun ordenarProjetos(projetos: List<ProjetoResumo>): List<ProjetoResumo> =
        projetos.sortedWith(
            compareBy<ProjetoResumo> { it.projeto.estagio == EstagioProjeto.FINALIZADO }
                .thenBy(collator) { it.projeto.titulo }
        )

    // Emite a data de hoje e de novo a cada meia-noite: atrasado, parado e "dias no estágio" não
    // ficam presos ao dia em que a tela abriu
    private fun diaAtual(): Flow<LocalDate> = flow {
        while (true) {
            val agora = LocalDateTime.now()
            emit(agora.toLocalDate())
            val proximaMeiaNoite = agora.toLocalDate().plusDays(1).atStartOfDay()
            delay(Duration.between(agora, proximaMeiaNoite).toMillis() + 1_000)
        }
    }
}
