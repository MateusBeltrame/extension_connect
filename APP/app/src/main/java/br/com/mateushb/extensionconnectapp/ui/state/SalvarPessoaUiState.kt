package br.com.mateushb.extensionconnectapp.ui.state

import br.com.mateushb.extensionconnectapp.model.BloqueioEdicaoPessoa
import br.com.mateushb.extensionconnectapp.model.Usuario

sealed interface SalvarPessoaUiState {
    data object Ocioso : SalvarPessoaUiState
    data object Salvando : SalvarPessoaUiState
    data class Sucesso(val usuario: Usuario) : SalvarPessoaUiState

    // Um projeto mudou desde que a tela abriu: a tela mostra o diálogo de bloqueio com os dados atuais
    data class Bloqueado(val bloqueio: BloqueioEdicaoPessoa) : SalvarPessoaUiState
    data class Erro(val mensagem: String) : SalvarPessoaUiState
}
