package br.com.mateushb.extensionconnectapp.ui.state

sealed interface RecuperarSenhaUiState {
    data object Ocioso : RecuperarSenhaUiState
    data object Enviado : RecuperarSenhaUiState
}
