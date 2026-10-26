package br.com.mateushb.extensionconnectapp.model

// Filtros da tela Pessoas (spec 007): chip de perfil (null = "Todos") e "Mostrar inativos".
// Não são lembrados entre execuções
data class FiltroPessoas(
    val perfil: PerfilUsuario? = null,
    val mostrarInativos: Boolean = false
) {
    fun atende(usuario: Usuario): Boolean =
        (perfil == null || usuario.perfil == perfil) && (mostrarInativos || usuario.ativo)
}
