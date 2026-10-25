package br.com.mateushb.extensionconnectapp.ui.telas

import android.util.Patterns
import br.com.mateushb.extensionconnectapp.model.Usuario

// Regras de validação compartilhadas pelas telas de acesso (spec 001)
internal object ValidacaoAcesso {

    const val DOMINIO_INSTITUCIONAL = "@alunos.afya.com.br"
    const val TAMANHO_MINIMO_SENHA = 6

    fun erroEmail(email: String, exigirDominio: Boolean = false): String? {
        val normalizado = Usuario.normalizarEmail(email)
        return when {
            normalizado.isEmpty() -> "Informe o e-mail"
            !Patterns.EMAIL_ADDRESS.matcher(normalizado).matches() -> "E-mail inválido"
            exigirDominio && !normalizado.endsWith(DOMINIO_INSTITUCIONAL) -> "Use seu e-mail institucional $DOMINIO_INSTITUCIONAL"
            else -> null
        }
    }
}
