package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.Projeto

sealed interface ProjetosPerfilUiState {
    data object Carregando : ProjetosPerfilUiState
    data class Sucesso(val projetos: List<Projeto>) : ProjetosPerfilUiState
    data class Erro(val mensagem: String) : ProjetosPerfilUiState
}
