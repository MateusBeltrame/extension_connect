package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.repository.UsuarioRepository
import br.com.mateushb.extensionconnectapp.model.ResultadoTrocarSenha
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.TrocarSenhaUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

// Trocar senha (spec 008). Uma instância por entrada da pilha (criada no composable da rota)
class TrocarSenhaViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: UsuarioRepository = UsuarioRepository(
        AppDatabase.obterInstancia(application).usuarioDao()
    )
) : AndroidViewModel(application) {

    var trocarSenhaUiState: TrocarSenhaUiState by mutableStateOf(TrocarSenhaUiState.Ocioso)
        private set

    fun trocarSenha(usuario: Usuario, senhaAtual: String, novaSenha: String) {
        if (trocarSenhaUiState is TrocarSenhaUiState.Salvando) return
        trocarSenhaUiState = TrocarSenhaUiState.Salvando
        viewModelScope.launch {
            trocarSenhaUiState = try {
                when (repository.trocarSenha(usuario.id, senhaAtual, novaSenha)) {
                    ResultadoTrocarSenha.Sucesso -> TrocarSenhaUiState.Sucesso
                    ResultadoTrocarSenha.SenhaAtualIncorreta -> TrocarSenhaUiState.SenhaAtualIncorreta
                    ResultadoTrocarSenha.NaoEncontrado ->
                        TrocarSenhaUiState.Erro("Usuário não encontrado no banco local.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                TrocarSenhaUiState.Erro("Erro ao alterar a senha no banco local. Tente novamente.")
            }
        }
    }

    fun limparTrocarSenhaUiState() {
        trocarSenhaUiState = TrocarSenhaUiState.Ocioso
    }
}
