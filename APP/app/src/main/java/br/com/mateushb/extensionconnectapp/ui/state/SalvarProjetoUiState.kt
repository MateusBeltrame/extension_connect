package br.com.mateushb.extensionconnectapp.ui.state

sealed interface SalvarProjetoUiState {
    data object Ocioso : SalvarProjetoUiState
    data object Salvando : SalvarProjetoUiState
    data class Sucesso(val projetoId: Int) : SalvarProjetoUiState
    data class Erro(val mensagem: String) : SalvarProjetoUiState
}
