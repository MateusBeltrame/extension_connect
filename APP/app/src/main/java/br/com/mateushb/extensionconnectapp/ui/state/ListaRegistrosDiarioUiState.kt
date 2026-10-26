package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.RegistroDiarioDetalhes

sealed interface ListaRegistrosDiarioUiState {
    data object Carregando : ListaRegistrosDiarioUiState
    data class Sucesso(val registros: List<RegistroDiarioDetalhes>) : ListaRegistrosDiarioUiState
    data class Erro(val mensagem: String) : ListaRegistrosDiarioUiState
}
