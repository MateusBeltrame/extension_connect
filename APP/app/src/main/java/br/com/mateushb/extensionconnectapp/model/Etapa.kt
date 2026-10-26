package br.com.mateushb.extensionconnectapp.model

import java.time.LocalDate

data class Etapa(
    val id: Int = 0,
    val projetoId: Int = 0,
    val nome: String = "",
    val dataPrevista: String = "",
    val concluida: Boolean = false,
    val ordem: Int = 0
) {
    // Data inválida não conta como atraso
    fun atrasada(hoje: LocalDate): Boolean {
        if (concluida) return false
        val prevista = lerDataIso(dataPrevista) ?: return false
        return hoje.isAfter(prevista)
    }
}
