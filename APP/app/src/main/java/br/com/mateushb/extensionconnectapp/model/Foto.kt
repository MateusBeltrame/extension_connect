package br.com.mateushb.extensionconnectapp.model

// Foto do projeto (spec 006). autorizadaMural é decisão explícita no envio (sem valor marcado de
// início) e só o autor muda depois; o Mural (feature 9) mostra só as autorizadas de projeto finalizado
data class Foto(
    val id: Int = 0,
    val projetoId: Int = 0,
    val autorId: Int = 0,
    val nomeArquivo: String = "",
    // Tamanho já comprimida (JPEG)
    val tamanhoBytes: Long = 0,
    val caminhoLocal: String = "",
    val dataEnvio: String = "",
    val legenda: String? = null,
    val autorizadaMural: Boolean = false,
    val statusEnvio: StatusEnvio = StatusEnvio.AGUARDANDO_ENVIO,
    val progresso: Int? = null
)
