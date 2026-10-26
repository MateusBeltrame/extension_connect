package br.com.mateushb.extensionconnectapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.repository.ProjetoRepository
import br.com.mateushb.extensionconnectapp.ui.state.DetalhesProjetoUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// Escopo: a entrada ProjetoDetalhes da pilha. Separado do ProjetoViewModel (lista e filtros) para a
// tela de detalhes não montar estado de lista nem ler as preferências de filtro; cresce na feature 4
class ProjetoDetalhesViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ProjetoRepository = AppDatabase.obterInstancia(application).let {
        ProjetoRepository(it.projetoDao(), it.usuarioDao())
    }
) : AndroidViewModel(application) {

    // Estados da tela de detalhes (provisória até a feature 4)
    var detalhesUiState: DetalhesProjetoUiState by mutableStateOf(DetalhesProjetoUiState.Carregando)
        private set

    private var detalhesJob: Job? = null

    // ===== Operações da tela de detalhes =====

    // Também é o "Tentar novamente"
    fun carregarDetalhesProjeto(projetoId: Int) {
        detalhesJob?.cancel()
        detalhesUiState = DetalhesProjetoUiState.Carregando
        detalhesJob = viewModelScope.launch {
            repository.projetoEmTempoReal(projetoId)
                .catch { detalhesUiState = DetalhesProjetoUiState.Erro("Erro ao carregar projeto. Tente novamente.") }
                .collect { projeto ->
                    detalhesUiState = if (projeto != null) {
                        DetalhesProjetoUiState.Sucesso(projeto)
                    } else {
                        DetalhesProjetoUiState.Erro("Projeto não encontrado no banco local.")
                    }
                }
        }
    }
}
