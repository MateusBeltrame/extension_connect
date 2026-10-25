package br.com.mateushb.extensionconnectapp.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {

    // Agrupamento de Telas: Autenticação
    @Serializable
    data object Acesso : AppRoute

    @Serializable
    data object RecuperarSenha : AppRoute

    @Serializable
    data object CompletarCadastro : AppRoute

    // Agrupamento de Telas: Projetos
    @Serializable
    data object ProjetosLista : AppRoute
}
