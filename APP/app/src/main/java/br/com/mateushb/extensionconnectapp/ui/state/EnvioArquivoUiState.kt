package br.com.mateushb.extensionconnectapp.ui.state

// Pôr um documento ou uma foto na fila de envio (spec 006). Mensagens no estado, como no diário
sealed interface EnvioArquivoUiState {
    data object Ocioso : EnvioArquivoUiState
    data object Enviando : EnvioArquivoUiState
    data class Sucesso(val mensagem: String) : EnvioArquivoUiState

    // Projeto finalizado ou usuário fora do projeto desde que a tela leu: nada foi gravado
    data class SemPermissao(val mensagem: String) : EnvioArquivoUiState
    data class Erro(val mensagem: String) : EnvioArquivoUiState
}
