package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.CandidatoParticipante
import br.com.mateushb.extensionconnectapp.model.Usuario

sealed interface FormularioProjetoUiState {
    data object Carregando : FormularioProjetoUiState
    data class Sucesso(
        val professores: List<Usuario>,
        val candidatos: List<CandidatoParticipante>
    ) : FormularioProjetoUiState
    data class Erro(val mensagem: String) : FormularioProjetoUiState
}
