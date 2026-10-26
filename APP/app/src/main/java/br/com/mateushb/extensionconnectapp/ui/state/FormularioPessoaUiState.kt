package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.PessoaDetalhes

sealed interface FormularioPessoaUiState {
    data object Ocioso : FormularioPessoaUiState
    data object Carregando : FormularioPessoaUiState
    data class Sucesso(val detalhes: PessoaDetalhes) : FormularioPessoaUiState
    data class Erro(val mensagem: String) : FormularioPessoaUiState
}
