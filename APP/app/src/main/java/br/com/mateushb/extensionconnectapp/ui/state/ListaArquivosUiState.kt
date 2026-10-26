package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.ArquivosProjeto

sealed interface ListaArquivosUiState {
    data object Carregando : ListaArquivosUiState
    data class Sucesso(val arquivos: ArquivosProjeto) : ListaArquivosUiState
    data class Erro(val mensagem: String) : ListaArquivosUiState
}
