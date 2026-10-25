package br.com.mateushb.extensionconnectapp.data.local

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Hash de senha local (PBKDF2 + sal aleatório), gravado como "iteracoes:sal:hash" (sal e hash em
// Base64). Guardar as iterações permite aumentá-las no futuro sem invalidar as senhas existentes
object HashSenha {

    private const val ALGORITMO = "PBKDF2WithHmacSHA256"
    // Recomendação OWASP (2023) para PBKDF2-HMAC-SHA256
    private const val ITERACOES = 600_000
    private const val TAMANHO_CHAVE_BITS = 256
    private const val TAMANHO_SAL_BYTES = 16

    fun gerar(senha: String): String {
        val sal = ByteArray(TAMANHO_SAL_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = derivar(senha, sal)
        return "$ITERACOES:${codificar(sal)}:${codificar(hash)}"
    }

    fun verificar(senha: String, senhaHash: String): Boolean {
        val partes = senhaHash.split(":")
        if (partes.size != 3) return false
        return try {
            val iteracoes = partes[0].toInt()
            val sal = Base64.decode(partes[1], Base64.NO_WRAP)
            val esperado = Base64.decode(partes[2], Base64.NO_WRAP)
            MessageDigest.isEqual(derivar(senha, sal, iteracoes), esperado)
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun derivar(senha: String, sal: ByteArray, iteracoes: Int = ITERACOES): ByteArray {
        val spec = PBEKeySpec(senha.toCharArray(), sal, iteracoes, TAMANHO_CHAVE_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun codificar(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
