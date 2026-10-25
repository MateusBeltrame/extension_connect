package br.com.mateushb.extensionconnectapp.model

// Log append-only: uma linha por mudança de estágio (a primeira é a criação do projeto)
data class HistoricoEstagio(
    val id: Int = 0,
    val projetoId: Int = 0,
    val estagio: EstagioProjeto = EstagioProjeto.EM_ANALISE,
    val autorId: Int = 0,
    val data: String = "",
    val justificativa: String? = null
)
