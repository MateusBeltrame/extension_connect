package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.Usuario

sealed interface SalvarPerfilUiState {
    data object Ocioso : SalvarPerfilUiState
    data object Salvando : SalvarPerfilUiState
    data class Sucesso(val usuario: Usuario) : SalvarPerfilUiState
    data class Erro(val mensagem: String) : SalvarPerfilUiState
}
