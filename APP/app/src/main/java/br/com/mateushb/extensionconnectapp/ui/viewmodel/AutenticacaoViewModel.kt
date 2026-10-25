package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.local.preferences.AppPreferencesManager
import br.com.mateushb.extensionconnectapp.data.repository.UsuarioRepository
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.ResultadoLogin
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.CadastroUiState
import br.com.mateushb.extensionconnectapp.ui.state.CompletarCadastroUiState
import br.com.mateushb.extensionconnectapp.ui.state.LoginUiState
import br.com.mateushb.extensionconnectapp.ui.state.RecuperarSenhaUiState
import br.com.mateushb.extensionconnectapp.ui.state.SessaoUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AutenticacaoViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: UsuarioRepository = UsuarioRepository(
        AppDatabase.obterInstancia(application).usuarioDao()
    ),
    private val preferencesManager: AppPreferencesManager = AppPreferencesManager(application)
) : AndroidViewModel(application) {

    // Sessão (usuário logado para todo o app)
    var sessaoUiState: SessaoUiState by mutableStateOf(SessaoUiState.Verificando)
        private set

    // Estados da tela de acesso (abas Entrar / Cadastrar-se)
    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Ocioso)
        private set

    var cadastroUiState: CadastroUiState by mutableStateOf(CadastroUiState.Ocioso)
        private set

    // Estado da tela de recuperar senha
    var recuperarSenhaUiState: RecuperarSenhaUiState by mutableStateOf(RecuperarSenhaUiState.Ocioso)
        private set

    // Estado da tela de completar cadastro
    var completarCadastroUiState: CompletarCadastroUiState by mutableStateOf(CompletarCadastroUiState.Ocioso)
        private set

    val usuarioLogado: Usuario?
        get() = (sessaoUiState as? SessaoUiState.Autenticado)?.usuario

    init {
        verificarSessao()
    }

    // ===== Sessão =====

    private fun verificarSessao() {
        val usuarioId = preferencesManager.obterUsuarioLogadoId()
        if (usuarioId == null) {
            sessaoUiState = SessaoUiState.NaoAutenticado
            return
        }
        viewModelScope.launch {
            sessaoUiState = try {
                val usuario = repository.obterPorId(usuarioId)
                // Conta removida ou desativada desde o último acesso: descarta a sessão lembrada
                if (usuario == null || !usuario.ativo) {
                    preferencesManager.limparSessao()
                    SessaoUiState.NaoAutenticado
                } else {
                    SessaoUiState.Autenticado(usuario)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                SessaoUiState.NaoAutenticado
            }
        }
    }

    private fun iniciarSessao(usuario: Usuario) {
        preferencesManager.salvarUsuarioLogadoId(usuario.id)
        sessaoUiState = SessaoUiState.Autenticado(usuario)
    }

    fun sair() {
        preferencesManager.limparSessao()
        sessaoUiState = SessaoUiState.NaoAutenticado
        loginUiState = LoginUiState.Ocioso
        cadastroUiState = CadastroUiState.Ocioso
        completarCadastroUiState = CompletarCadastroUiState.Ocioso
    }

    // ===== Operações da aba Entrar =====

    fun entrar(email: String, senha: String) {
        if (loginUiState is LoginUiState.Entrando) return
        loginUiState = LoginUiState.Entrando
        viewModelScope.launch {
            loginUiState = try {
                when (val resultado = repository.autenticar(email, senha)) {
                    is ResultadoLogin.Sucesso -> {
                        iniciarSessao(resultado.usuario)
                        LoginUiState.Sucesso
                    }
                    ResultadoLogin.CredenciaisInvalidas ->
                        LoginUiState.Erro("E-mail ou senha incorretos.")
                    ResultadoLogin.ContaDesativada ->
                        LoginUiState.Erro("Sua conta está desativada. Procure a coordenação.", contaDesativada = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                LoginUiState.Erro("Erro ao acessar o banco local. Tente novamente.")
            }
        }
    }

    fun limparLoginUiState() {
        loginUiState = LoginUiState.Ocioso
    }

    // ===== Operações da aba Cadastrar-se =====

    fun cadastrar(nome: String, email: String, senha: String) {
        if (cadastroUiState is CadastroUiState.Cadastrando) return
        cadastroUiState = CadastroUiState.Cadastrando
        viewModelScope.launch {
            cadastroUiState = try {
                if (repository.emailJaCadastrado(email)) {
                    CadastroUiState.EmailJaCadastrado
                } else {
                    iniciarSessao(repository.cadastrarAluno(nome, email, senha))
                    CadastroUiState.Sucesso
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: SQLiteConstraintException) {
                // Outro cadastro com o mesmo e-mail entrou entre a verificação e o insert (índice único)
                CadastroUiState.EmailJaCadastrado
            } catch (_: Exception) {
                CadastroUiState.Erro("Erro ao criar a conta no banco local. Tente novamente.")
            }
        }
    }

    fun limparCadastroUiState() {
        cadastroUiState = CadastroUiState.Ocioso
    }

    // ===== Operações da tela de recuperar senha =====

    // Sem backend de e-mail: a resposta é sempre neutra e não consulta o banco (spec 001, HU-03)
    fun enviarLinkRecuperacao(@Suppress("UNUSED_PARAMETER") email: String) {
        // Síncrono de propósito: não há envio real, então não existe estado intermediário "enviando"
        recuperarSenhaUiState = RecuperarSenhaUiState.Enviado
    }

    fun limparRecuperarSenhaUiState() {
        recuperarSenhaUiState = RecuperarSenhaUiState.Ocioso
    }

    // ===== Operações da tela de completar cadastro =====

    fun completarCadastro(ra: String, curso: Curso) {
        val usuario = usuarioLogado ?: return
        if (completarCadastroUiState is CompletarCadastroUiState.Salvando) return
        completarCadastroUiState = CompletarCadastroUiState.Salvando
        viewModelScope.launch {
            completarCadastroUiState = try {
                sessaoUiState = SessaoUiState.Autenticado(repository.completarCadastro(usuario.id, ra, curso))
                CompletarCadastroUiState.Sucesso
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                CompletarCadastroUiState.Erro("Erro ao salvar o cadastro no banco local. Tente novamente.")
            }
        }
    }

    fun limparCompletarCadastroUiState() {
        completarCadastroUiState = CompletarCadastroUiState.Ocioso
    }
}
