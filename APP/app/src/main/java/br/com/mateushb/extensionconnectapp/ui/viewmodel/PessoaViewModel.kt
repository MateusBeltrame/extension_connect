package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.repository.UsuarioRepository
import br.com.mateushb.extensionconnectapp.model.FiltroPessoas
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.ResultadoSalvarPessoa
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.FormularioPessoaUiState
import br.com.mateushb.extensionconnectapp.ui.state.ListaPessoasUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarPessoaUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

// Tela Pessoas e Editar pessoa (spec 007). Uma instância por entrada da pilha (criada no composable de
// cada rota): a lista só observa o banco quando carregarPessoas() é chamado
class PessoaViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: UsuarioRepository = UsuarioRepository(
        AppDatabase.obterInstancia(application).usuarioDao()
    )
) : AndroidViewModel(application) {

    // Estados da tela de listagem. Busca e filtros não são lembrados entre execuções
    var listaUiState: ListaPessoasUiState by mutableStateOf(ListaPessoasUiState.Carregando)
        private set

    var busca: String by mutableStateOf("")
        private set

    var filtroAtual: FiltroPessoas by mutableStateOf(FiltroPessoas())
        private set

    private val buscaFlow = MutableStateFlow(busca)
    private val filtroFlow = MutableStateFlow(filtroAtual)

    // Estados da tela de formulário
    var formularioUiState: FormularioPessoaUiState by mutableStateOf(FormularioPessoaUiState.Ocioso)
        private set

    var salvarUiState: SalvarPessoaUiState by mutableStateOf(SalvarPessoaUiState.Ocioso)
        private set

    private var pessoasJob: Job? = null
    private var pessoaCarregadaId: Int? = null

    private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR")).apply {
        // Ignora acento e caixa na ordem por nome ("Álvaro" junto de "Alberto")
        strength = Collator.PRIMARY
    }

    // ===== Operações da tela de listagem =====

    // Também é o "Tentar novamente". Não reinicia se já observa sem erro (recomposição da rota)
    fun carregarPessoas() {
        if (pessoasJob?.isActive == true && listaUiState !is ListaPessoasUiState.Erro) return
        pessoasJob?.cancel()
        listaUiState = ListaPessoasUiState.Carregando
        // Ordem e chave de busca calculadas uma vez por mudança no banco, não a cada letra digitada
        val pessoasOrdenadas = repository.usuariosEmTempoReal
            .map { usuarios ->
                usuarios.sortedWith(compareBy(collator) { it.nome }).map { it to it.chaveBusca }
            }
            .flowOn(Dispatchers.Default)
        pessoasJob = viewModelScope.launch {
            combine(pessoasOrdenadas, buscaFlow, filtroFlow) { pessoas, texto, filtro ->
                val termo = ProjetoResumo.normalizar(texto)
                ListaPessoasUiState.Sucesso(
                    pessoas = pessoas
                        .filter { (usuario, chave) -> filtro.atende(usuario) && chave.contains(termo) }
                        .map { it.first },
                    total = pessoas.size
                ) as ListaPessoasUiState
            }
                .flowOn(Dispatchers.Default)
                .catch { emit(ListaPessoasUiState.Erro("Erro ao carregar pessoas do banco local. Tente novamente.")) }
                .collect { listaUiState = it }
        }
    }

    fun alterarBusca(texto: String) {
        busca = texto
        buscaFlow.value = texto
    }

    fun aplicarFiltro(filtro: FiltroPessoas) {
        filtroAtual = filtro
        filtroFlow.value = filtro
    }

    // ===== Operações da tela de formulário =====

    // Na rotação o LaunchedEffect roda de novo: o mesmo id já carregado não é relido, para não apagar o
    // que foi digitado. Depois de um erro, recarrega ("Tentar novamente")
    fun carregarPessoa(id: Int) {
        if (pessoaCarregadaId == id && formularioUiState !is FormularioPessoaUiState.Erro) return
        pessoaCarregadaId = id
        salvarUiState = SalvarPessoaUiState.Ocioso
        formularioUiState = FormularioPessoaUiState.Carregando
        viewModelScope.launch {
            formularioUiState = try {
                val detalhes = repository.obterPessoaDetalhes(id)
                if (detalhes == null) {
                    FormularioPessoaUiState.Erro("Pessoa não encontrada no banco local.")
                } else {
                    FormularioPessoaUiState.Sucesso(detalhes)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                FormularioPessoaUiState.Erro("Erro ao carregar pessoa. Tente novamente.")
            }
        }
    }

    // editor = Administrador logado, conferido no banco para a regra do próprio cadastro (HU-05)
    fun salvar(editor: Usuario, pessoa: Usuario) {
        if (salvarUiState is SalvarPessoaUiState.Salvando) return
        salvarUiState = SalvarPessoaUiState.Salvando
        viewModelScope.launch {
            salvarUiState = try {
                when (val resultado = repository.salvarPessoa(editor, pessoa)) {
                    is ResultadoSalvarPessoa.Sucesso -> SalvarPessoaUiState.Sucesso(resultado.usuario)
                    is ResultadoSalvarPessoa.Bloqueado -> SalvarPessoaUiState.Bloqueado(resultado.bloqueio)
                    ResultadoSalvarPessoa.NaoEncontrada ->
                        SalvarPessoaUiState.Erro("Pessoa não encontrada no banco local.")
                    ResultadoSalvarPessoa.EdicaoPropriaProibida ->
                        SalvarPessoaUiState.Erro("Você não pode alterar seu próprio perfil nem se desativar.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                SalvarPessoaUiState.Erro("Erro ao salvar pessoa no banco local. Tente novamente.")
            }
            // O banco recusou porque um projeto mudou com a tela aberta: as próximas conferências da
            // tela passam a usar os vínculos atuais
            if (salvarUiState is SalvarPessoaUiState.Bloqueado) recarregarBloqueios()
        }
    }

    // Relê os vínculos com projetos sem reiniciar o formulário (o que foi digitado fica, pois a tela
    // guarda os campos pelo id da pessoa). Chamado após um bloqueio, local ou do banco: um projeto pode
    // ter sido criado ou finalizado depois que a tela abriu
    fun recarregarBloqueios() {
        val id = pessoaCarregadaId ?: return
        if (formularioUiState !is FormularioPessoaUiState.Sucesso) return
        viewModelScope.launch {
            try {
                repository.obterPessoaDetalhes(id)?.let { formularioUiState = FormularioPessoaUiState.Sucesso(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Mantém os vínculos já carregados; o banco confere de novo ao salvar
            }
        }
    }

    fun limparSalvarUiState() {
        salvarUiState = SalvarPessoaUiState.Ocioso
    }
}
