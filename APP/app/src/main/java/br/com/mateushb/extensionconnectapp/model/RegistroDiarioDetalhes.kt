package br.com.mateushb.extensionconnectapp.model

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Registro com o nome do autor e a etapa relacionada (null se não houver ou se ela foi excluída)
data class RegistroDiarioDetalhes(
    val registro: RegistroDiario,
    val autor: Usuario? = null,
    val etapa: Etapa? = null
) {
    // "Hoje, 14:20" · "Ontem, 18:05" · "12/11, 10:30" · "12/11/2025, 10:30", no fuso do aparelho.
    // Data inválida vira texto vazio (um registro inconsistente não derruba a lista)
    fun dataExibida(agora: LocalDateTime, zona: ZoneId = ZoneId.systemDefault()): String {
        val momento = lerInstanteIso(registro.data)?.atZone(zona)?.toLocalDateTime() ?: return ""
        val hora = momento.format(FORMATO_HORA)
        val dia = momento.toLocalDate()
        val hoje = agora.toLocalDate()
        return when {
            dia == hoje -> "Hoje, $hora"
            dia == hoje.minusDays(1) -> "Ontem, $hora"
            dia.year == hoje.year -> "${dia.format(FORMATO_DIA_MES)}, $hora"
            else -> "${dia.format(FORMATO_DATA)}, $hora"
        }
    }

    private companion object {
        val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val FORMATO_DIA_MES: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
        val FORMATO_DATA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}
