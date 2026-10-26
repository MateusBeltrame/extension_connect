package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.Projeto

sealed interface DetalhesProjetoUiState {
    data object Carregando : DetalhesProjetoUiState
    data class Sucesso(val projeto: Projeto) : DetalhesProjetoUiState
    data class Erro(val mensagem: String) : DetalhesProjetoUiState
}
