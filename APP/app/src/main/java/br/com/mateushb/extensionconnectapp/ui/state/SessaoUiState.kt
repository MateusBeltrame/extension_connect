package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.Usuario

sealed interface SessaoUiState {
    data object Verificando : SessaoUiState
    data object NaoAutenticado : SessaoUiState
    data class Autenticado(val usuario: Usuario) : SessaoUiState
}
