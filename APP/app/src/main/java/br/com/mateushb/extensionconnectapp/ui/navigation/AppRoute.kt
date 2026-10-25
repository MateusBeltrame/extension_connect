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

    // Agrupamento de Telas: Pessoas (provisória até a feature 7)
    @Serializable
    data object PessoasLista : AppRoute

    // Agrupamento de Telas: Perfil (provisória até a feature 8)
    @Serializable
    data object MeuPerfil : AppRoute

    // Agrupamento de Telas: Mural (provisória até a feature 9)
    @Serializable
    data object Mural : AppRoute
}
