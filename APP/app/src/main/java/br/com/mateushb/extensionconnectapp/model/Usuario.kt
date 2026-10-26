package br.com.mateushb.extensionconnectapp.model

// Sem @SerializedName: o ExtensionConnect não tem Gson/API remota (ver 07-dados.md)
data class Usuario(
    val id: Int = 0,
    val nome: String = "",
    val email: String = "",
    val senhaHash: String = "",
    val perfil: PerfilUsuario = PerfilUsuario.ALUNO,
    val ativo: Boolean = true,
    val ra: String? = null,
    val curso: Curso? = null,
    val aceitouTermosEm: String? = null
) {
    val primeiroNome: String
        get() = nome.trim().substringBefore(' ')

    // Aluno sem RA/curso precisa passar pela tela "Completar cadastro"
    val cadastroIncompleto: Boolean
        get() = perfil == PerfilUsuario.ALUNO && (ra.isNullOrBlank() || curso == null)

    // Nome e e-mail normalizados (ProjetoResumo.normalizar) numa só chave; o termo da busca é de uma
    // linha, então nunca casa atravessando o "\n". Quem filtra muitas vezes a mesma lista guarda a chave
    val chaveBusca: String
        get() = ProjetoResumo.normalizar(nome) + "\n" + ProjetoResumo.normalizar(email)

    // Termo já normalizado (busca do wizard "Novo projeto" e da tela Pessoas)
    fun correspondeABusca(termo: String): Boolean = chaveBusca.contains(termo)

    companion object {
        // E-mail é comparado e gravado sem espaços nas pontas e em minúsculas
        fun normalizarEmail(email: String): String = email.trim().lowercase()
    }
}
