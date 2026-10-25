package br.com.mateushb.extensionconnectapp.ui.state

sealed interface CadastroUiState {
    data object Ocioso : CadastroUiState
    data object Cadastrando : CadastroUiState
    data object Sucesso : CadastroUiState
    data object EmailJaCadastrado : CadastroUiState
    data class Erro(val mensagem: String) : CadastroUiState
}
