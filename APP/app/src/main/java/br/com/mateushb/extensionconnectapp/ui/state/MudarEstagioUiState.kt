package br.com.mateushb.extensionconnectapp.ui.state

sealed interface MudarEstagioUiState {
    data object Ocioso : MudarEstagioUiState
    data object Salvando : MudarEstagioUiState
    data class Sucesso(val mensagem: String) : MudarEstagioUiState
    data class EstagioMudou(val mensagem: String) : MudarEstagioUiState
    data class Erro(val mensagem: String) : MudarEstagioUiState
}
