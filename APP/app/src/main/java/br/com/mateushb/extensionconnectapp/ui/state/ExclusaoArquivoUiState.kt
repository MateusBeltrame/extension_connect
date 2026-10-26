package br.com.mateushb.extensionconnectapp.ui.state

// Excluir um documento ou uma foto (spec 006, HU-07 e HU-08)
sealed interface ExclusaoArquivoUiState {
    data object Ocioso : ExclusaoArquivoUiState
    data object Excluindo : ExclusaoArquivoUiState
    data class Sucesso(val mensagem: String) : ExclusaoArquivoUiState
    data class Erro(val mensagem: String) : ExclusaoArquivoUiState
}
