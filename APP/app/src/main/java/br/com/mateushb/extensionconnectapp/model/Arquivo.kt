package br.com.mateushb.extensionconnectapp.model

// Documento do projeto (spec 006). Só é criado (pela fila de envio) e, pelo próprio autor, excluído
data class Arquivo(
    val id: Int = 0,
    val projetoId: Int = 0,
    val autorId: Int = 0,
    val nomeArquivo: String = "",
    val extensao: FormatoDocumento = FormatoDocumento.PDF,
    val tamanhoBytes: Long = 0,
    // Cópia no armazenamento do app; vazio nos dados de exemplo (não há arquivo no aparelho)
    val caminhoLocal: String = "",
    // Instant ISO (Instant.now().toString()), preenchido pelo Repository
    val dataEnvio: String = "",
    val statusEnvio: StatusEnvio = StatusEnvio.AGUARDANDO_ENVIO,
    // 0–100, só enquanto ENVIANDO
    val progresso: Int? = null
) {
    companion object {
        // Vale para o documento como está e para a foto depois de comprimida (spec 006)
        const val TAMANHO_MAXIMO: Long = 10L * 1024 * 1024
    }
}
