package br.com.mateushb.extensionconnectapp.ui.state

sealed interface CompletarCadastroUiState {
    data object Ocioso : CompletarCadastroUiState
    data object Salvando : CompletarCadastroUiState
    data object Sucesso : CompletarCadastroUiState
    data class Erro(val mensagem: String) : CompletarCadastroUiState
}
