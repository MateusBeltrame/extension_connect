package br.com.mateushb.extensionconnectapp.model

// Filtros do bottom sheet da tela Projetos; os valores padrão significam "sem filtro"
data class FiltroProjetos(
    val estagio: EstagioProjeto? = null,
    val tema: TemaProjeto? = null,
    val orientadorId: Int? = null,
    val souOrientador: Boolean = false,
    val soParados: Boolean = false
) {
    val ativo: Boolean
        get() = this != FiltroProjetos()
}
