package br.com.mateushb.extensionconnectapp.model

import java.time.LocalDate

// Foto com o nome de quem enviou, para o sheet da foto (spec 006, HU-07)
data class FotoDetalhes(
    val foto: Foto,
    val autor: Usuario? = null
) {
    fun legendaExibida(hoje: LocalDate): String =
        legendaArquivo(foto.tamanhoBytes, autor?.nome, foto.dataEnvio, hoje)
}
