package br.com.mateushb.extensionconnectapp.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Leitura tolerante das datas gravadas como texto ISO: valor ausente ou inválido vira null, nunca
// exceção (um registro inconsistente não derruba a tela)

fun lerDataIso(iso: String?): LocalDate? =
    if (iso.isNullOrBlank()) {
        null
    } else {
        try {
            LocalDate.parse(iso)
        } catch (_: Exception) {
            null
        }
    }

fun lerInstanteIso(iso: String?): Instant? =
    if (iso.isNullOrBlank()) {
        null
    } else {
        try {
            Instant.parse(iso)
        } catch (_: Exception) {
            null
        }
    }

// Instant gravado em UTC → dia no fuso do aparelho
fun dataLocalDoInstante(iso: String?): LocalDate? =
    lerInstanteIso(iso)?.atZone(ZoneId.systemDefault())?.toLocalDate()

// "dd/MM" neste ano, "dd/MM/aaaa" em outro ano (linha de documento, spec 006); data inválida vira ""
fun dataCurta(iso: String?, hoje: LocalDate): String {
    val dia = dataLocalDoInstante(iso) ?: return ""
    return if (dia.year == hoje.year) dia.format(FORMATO_DIA_MES) else dia.format(FORMATO_DATA_COMPLETA)
}

// "dd/MM/aaaa" no fuso do aparelho ("Termos aceitos em", spec 008); data ausente ou inválida vira null
fun dataCompleta(iso: String?): String? = dataLocalDoInstante(iso)?.format(FORMATO_DATA_COMPLETA)

// "85 KB" abaixo de 1 MB (mínimo 1 KB) e "1,2 MB" a partir dele, na base 1024 como o Figma. Compara
// depois de arredondar: 1023,9 KB vira "1,0 MB", não "1024 KB"
fun formatarTamanho(bytes: Long): String {
    val kb = bytes / 1024.0
    val kbArredondado = Math.round(kb)
    return if (kbArredondado < 1024) {
        "${maxOf(1L, kbArredondado)} KB"
    } else {
        String.format(LOCALE_BRASIL, "%.1f MB", kb / 1024)
    }
}

private val FORMATO_DIA_MES: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
private val FORMATO_DATA_COMPLETA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val LOCALE_BRASIL: Locale = Locale.forLanguageTag("pt-BR")
