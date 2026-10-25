package br.com.mateushb.extensionconnectapp.model

// Ordem de declaração = ordem do ciclo de vida (e das colunas do quadro)
enum class EstagioProjeto(val titulo: String) {
    EM_ANALISE("Em análise"),
    EM_APLICACAO("Em aplicação"),
    FINALIZADO("Finalizado")
}
