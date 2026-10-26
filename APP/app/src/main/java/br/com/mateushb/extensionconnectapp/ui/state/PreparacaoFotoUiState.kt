package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.FotoPreparada

// Foto tirada ou escolhida sendo comprimida para o sheet "Autorizar divulgação" (spec 006, HU-06).
// Pronta continua até a foto ser enviada ou descartada; Erro é consumido pela tela (Snackbar)
sealed interface PreparacaoFotoUiState {
    data object Ocioso : PreparacaoFotoUiState
    data object Preparando : PreparacaoFotoUiState
    data class Pronta(val foto: FotoPreparada) : PreparacaoFotoUiState
    data class Erro(val mensagem: String) : PreparacaoFotoUiState
}
