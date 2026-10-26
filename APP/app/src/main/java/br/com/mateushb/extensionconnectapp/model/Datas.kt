package br.com.mateushb.extensionconnectapp.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
