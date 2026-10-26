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

    // Wizard de 3 passos: só cria (spec 003)
    @Serializable
    data object NovoProjeto : AppRoute

    // Provisória até a feature 4
    @Serializable
    data class ProjetoDetalhes(val projetoId: Int) : AppRoute

    // Agrupamento de Telas: Pessoas (spec 007, só Administrador)
    @Serializable
    data object PessoasLista : AppRoute

    // Só edita: não há "nova pessoa" (cadastro é pelo "Cadastrar-se")
    @Serializable
    data class EditarPessoa(val usuarioId: Int) : AppRoute

    // Agrupamento de Telas: Perfil (provisória até a feature 8)
    @Serializable
    data object MeuPerfil : AppRoute

    // Agrupamento de Telas: Mural (provisória até a feature 9)
    @Serializable
    data object Mural : AppRoute
}
