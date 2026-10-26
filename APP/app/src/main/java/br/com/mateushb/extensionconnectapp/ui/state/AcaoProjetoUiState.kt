package br.com.mateushb.extensionconnectapp.ui.state

// Ações rápidas dos detalhes (confirmar ciência, marcar etapa): só o resultado vira Snackbar
sealed interface AcaoProjetoUiState {
    data object Ocioso : AcaoProjetoUiState
    data class Sucesso(val mensagem: String) : AcaoProjetoUiState
    data class Erro(val mensagem: String) : AcaoProjetoUiState
}
