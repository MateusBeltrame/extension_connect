package br.com.mateushb.extensionconnectapp.model

import java.time.LocalDate

// Documento com o nome de quem enviou, para a linha do card Documentos (spec 006, HU-03)
data class ArquivoDetalhes(
    val arquivo: Arquivo,
    val autor: Usuario? = null
) {
    // "1,2 MB · Mateus Silva · 18/11"
    fun legendaExibida(hoje: LocalDate): String =
        legendaArquivo(arquivo.tamanhoBytes, autor?.nome, arquivo.dataEnvio, hoje)
}

// Partes vazias (autor ausente, data inválida) somem em vez de deixar "· ·" sobrando
internal fun legendaArquivo(tamanhoBytes: Long, nomeAutor: String?, dataEnvio: String, hoje: LocalDate): String =
    listOf(formatarTamanho(tamanhoBytes), nomeAutor.orEmpty(), dataCurta(dataEnvio, hoje))
        .filter { it.isNotBlank() }
        .joinToString(" · ")
