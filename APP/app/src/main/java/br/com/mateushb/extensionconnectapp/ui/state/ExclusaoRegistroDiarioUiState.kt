package br.com.mateushb.extensionconnectapp.ui.state

sealed interface ExclusaoRegistroDiarioUiState {
    data object Ocioso : ExclusaoRegistroDiarioUiState
    data object Excluindo : ExclusaoRegistroDiarioUiState
    data class Sucesso(val mensagem: String) : ExclusaoRegistroDiarioUiState
    data class Erro(val mensagem: String) : ExclusaoRegistroDiarioUiState
}
