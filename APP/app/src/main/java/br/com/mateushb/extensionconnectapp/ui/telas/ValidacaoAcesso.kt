package br.com.mateushb.extensionconnectapp.ui.telas

import android.util.Patterns
import br.com.mateushb.extensionconnectapp.model.Usuario

// Regras de validação compartilhadas pelas telas de acesso (spec 001) e de cadastro de pessoa (RA)
internal object ValidacaoAcesso {

    const val TAMANHO_MINIMO_SENHA = 6

    // Só 0-9: Char.isDigit também aceita dígitos de outros alfabetos ("１２３", "٢٠٢٣")
    fun somenteDigitos(texto: String): Boolean = texto.all { it in '0'..'9' }

    // RA: obrigatório em "Completar cadastro" e "Meu perfil", opcional em "Editar pessoa"; se preenchido,
    // só números (espaços nas pontas não contam)
    fun erroRa(ra: String, obrigatorio: Boolean): String? = when {
        ra.isBlank() -> if (obrigatorio) "Informe o RA" else null
        !somenteDigitos(ra.trim()) -> "Informe apenas números"
        else -> null
    }

    // Sem exigência de domínio: qualquer e-mail em formato válido é aceito
    fun erroEmail(email: String): String? {
        val normalizado = Usuario.normalizarEmail(email)
        return when {
            normalizado.isEmpty() -> "Informe o e-mail"
            !Patterns.EMAIL_ADDRESS.matcher(normalizado).matches() -> "E-mail inválido"
            else -> null
        }
    }
}
