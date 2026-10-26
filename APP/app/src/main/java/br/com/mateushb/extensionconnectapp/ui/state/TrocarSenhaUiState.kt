package br.com.mateushb.extensionconnectapp.ui.state

sealed interface TrocarSenhaUiState {
    data object Ocioso : TrocarSenhaUiState
    data object Salvando : TrocarSenhaUiState
    data object Sucesso : TrocarSenhaUiState

    // Mostrado no próprio campo "Senha atual" (frame 15b), não em Snackbar
    data object SenhaAtualIncorreta : TrocarSenhaUiState
    data class Erro(val mensagem: String) : TrocarSenhaUiState
}
