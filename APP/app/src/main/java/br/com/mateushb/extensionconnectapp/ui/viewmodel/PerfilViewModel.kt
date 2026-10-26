package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.repository.ProjetoRepository
import br.com.mateushb.extensionconnectapp.data.repository.UsuarioRepository
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.ProjetosPerfilUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarPerfilUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Meu perfil (spec 008). Uma instância por entrada da pilha (criada no composable da rota). Os dados do
// usuário vêm da sessão (AutenticacaoViewModel), que acompanha a linha no banco
class PerfilViewModel @JvmOverloads constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(
        AppDatabase.obterInstancia(application).usuarioDao()
    ),
    private val projetoRepository: ProjetoRepository = AppDatabase.obterInstancia(application).let {
        ProjetoRepository(it.projetoDao(), it.usuarioDao())
    }
) : AndroidViewModel(application) {

    var projetosUiState: ProjetosPerfilUiState by mutableStateOf(ProjetosPerfilUiState.Carregando)
        private set

    var salvarUiState: SalvarPerfilUiState by mutableStateOf(SalvarPerfilUiState.Ocioso)
        private set

    private var projetosJob: Job? = null
    private var projetosUsuarioId: Int? = null

    // ===== Card de projetos =====

    // Aluno: projeto atual. Professor: projetos que orienta. Administrador não tem card. Também é o
    // "Tentar novamente"; não reinicia se já observa o mesmo usuário sem erro (rotação)
    fun carregarProjetos(usuario: Usuario) {
        if (usuario.perfil == PerfilUsuario.ADMINISTRADOR) return
        if (projetosUsuarioId == usuario.id && projetosJob?.isActive == true &&
            projetosUiState !is ProjetosPerfilUiState.Erro
        ) return
        projetosJob?.cancel()
        projetosUsuarioId = usuario.id
        projetosUiState = ProjetosPerfilUiState.Carregando
        projetosJob = viewModelScope.launch {
            projetoRepository.projetosAtivosDoUsuario(usuario)
                .map { ProjetosPerfilUiState.Sucesso(it) as ProjetosPerfilUiState }
                .catch { emit(ProjetosPerfilUiState.Erro("Erro ao carregar seus projetos do banco local. Tente novamente.")) }
                .collect { projetosUiState = it }
        }
    }

    // ===== Meus dados =====

    // RA e curso são ignorados no banco se o usuário não for Aluno
    fun salvarMeusDados(usuario: Usuario, nome: String, ra: String?, curso: Curso?) {
        if (salvarUiState is SalvarPerfilUiState.Salvando) return
        salvarUiState = SalvarPerfilUiState.Salvando
        viewModelScope.launch {
            salvarUiState = try {
                SalvarPerfilUiState.Sucesso(usuarioRepository.salvarMeusDados(usuario.id, nome, ra, curso))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                SalvarPerfilUiState.Erro("Erro ao salvar seus dados no banco local. Tente novamente.")
            }
        }
    }

    fun limparSalvarUiState() {
        salvarUiState = SalvarPerfilUiState.Ocioso
    }
}
