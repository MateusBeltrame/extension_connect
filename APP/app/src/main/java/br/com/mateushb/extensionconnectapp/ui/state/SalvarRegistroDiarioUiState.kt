package br.com.mateushb.extensionconnectapp.ui.state

// Mensagens no estado, como no MudarEstagioUiState (spec 004): a tela só as exibe
sealed interface SalvarRegistroDiarioUiState {
    data object Ocioso : SalvarRegistroDiarioUiState
    data object Salvando : SalvarRegistroDiarioUiState
    data class Sucesso(val mensagem: String) : SalvarRegistroDiarioUiState

    // Projeto finalizado ou usuário fora do projeto desde que o sheet abriu: nada foi gravado
    data class SemPermissao(val mensagem: String) : SalvarRegistroDiarioUiState
    data class Erro(val mensagem: String) : SalvarRegistroDiarioUiState
}
