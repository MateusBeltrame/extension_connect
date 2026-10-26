package br.com.mateushb.extensionconnectapp.model

// Chips da aba Diário (frame 35:2). Não é persistido: a tela sempre abre em Todos
enum class FiltroDiario(val titulo: String, val tipo: TipoRegistro?, val mensagemVazio: String) {
    TODOS("Todos", null, "Nenhum registro no diário"),
    ATIVIDADES("Atividades", TipoRegistro.ATIVIDADE, "Nenhuma atividade registrada"),
    ORIENTACOES("Orientações", TipoRegistro.ORIENTACAO, "Nenhuma orientação registrada");

    fun aplicar(registros: List<RegistroDiarioDetalhes>): List<RegistroDiarioDetalhes> =
        if (tipo == null) registros else registros.filter { it.registro.tipo == tipo }
}
