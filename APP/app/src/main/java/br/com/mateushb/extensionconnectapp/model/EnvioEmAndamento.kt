package br.com.mateushb.extensionconnectapp.model

// O item que a FilaEnvio está enviando agora e o progresso dele (spec 006). Só existe em memória: o
// banco guarda apenas "pendente" ou "enviado", e "Enviando N%" / "sem internet" são calculados na
// leitura a partir disto e da conectividade. Assim cada passo do progresso não grava no banco
data class EnvioEmAndamento(
    val chave: String,
    val progresso: Int
) {
    companion object {
        fun chaveFoto(id: Int): String = "foto-$id"

        fun chaveDocumento(id: Int): String = "arquivo-$id"
    }
}
