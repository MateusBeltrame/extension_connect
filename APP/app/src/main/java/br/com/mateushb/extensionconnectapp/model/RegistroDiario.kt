package br.com.mateushb.extensionconnectapp.model

// Registro do diário do projeto (spec 005): só é criado e, pelo próprio autor, excluído
data class RegistroDiario(
    val id: Int = 0,
    val projetoId: Int = 0,
    val autorId: Int = 0,
    // Instant ISO (Instant.now().toString()), preenchido pelo Repository
    val data: String = "",
    val tipo: TipoRegistro = TipoRegistro.ATIVIDADE,
    val etapaId: Int? = null,
    val texto: String = ""
) {
    companion object {
        const val MIN_TEXTO = 10
        const val MAX_TEXTO = 1000

        // Conta sem os espaços das pontas; o limite máximo é aplicado no próprio campo
        fun erroTexto(texto: String): String? {
            val tamanho = texto.trim().length
            return when {
                tamanho < MIN_TEXTO -> "O registro deve ter ao menos $MIN_TEXTO caracteres"
                tamanho > MAX_TEXTO -> "O registro deve ter no máximo $MAX_TEXTO caracteres"
                else -> null
            }
        }
    }
}
