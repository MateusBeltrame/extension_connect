package br.com.mateushb.extensionconnectapp.ui.telas

import android.util.Patterns
import br.com.mateushb.extensionconnectapp.model.Usuario

// Regras de validação compartilhadas pelas telas de acesso (spec 001)
internal object ValidacaoAcesso {

    const val TAMANHO_MINIMO_SENHA = 6

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
