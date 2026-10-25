package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.Usuario
import java.time.LocalDate

sealed interface ListaProjetosUiState {
    data object Carregando : ListaProjetosUiState
    data class Sucesso(
        val projetos: List<ProjetoResumo>,
        val existemProjetos: Boolean,
        val orientadores: List<Usuario>,
        val hoje: LocalDate
    ) : ListaProjetosUiState
    data class Erro(val mensagem: String) : ListaProjetosUiState
}
