package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes

sealed interface DetalhesProjetoUiState {
    data object Carregando : DetalhesProjetoUiState
    data class Sucesso(val detalhes: ProjetoDetalhes) : DetalhesProjetoUiState
    data class Erro(val mensagem: String) : DetalhesProjetoUiState

    // Sem "Tentar novamente": repetir a consulta daria o mesmo resultado (HU-01 pede só o voltar)
    data class NaoEncontrado(val mensagem: String) : DetalhesProjetoUiState
}
