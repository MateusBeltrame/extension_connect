package br.com.mateushb.extensionconnectapp.ui.state

sealed interface LoginUiState {
    data object Ocioso : LoginUiState
    data object Entrando : LoginUiState
    data object Sucesso : LoginUiState
    data class Erro(val mensagem: String, val contaDesativada: Boolean = false) : LoginUiState
}
