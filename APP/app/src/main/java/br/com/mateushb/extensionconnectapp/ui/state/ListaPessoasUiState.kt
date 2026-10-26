package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.Usuario

sealed interface ListaPessoasUiState {
    data object Carregando : ListaPessoasUiState

    // pessoas: já filtradas e ordenadas; total: todas as cadastradas, sem filtro ("N pessoas cadastradas")
    data class Sucesso(val pessoas: List<Usuario>, val total: Int) : ListaPessoasUiState
    data class Erro(val mensagem: String) : ListaPessoasUiState
}
