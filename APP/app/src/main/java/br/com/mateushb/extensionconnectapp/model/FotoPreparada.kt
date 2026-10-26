package br.com.mateushb.extensionconnectapp.model

// Foto já comprimida, num arquivo temporário, esperando a resposta do sheet "Autorizar divulgação"
// (spec 006, HU-06). Enviar move o arquivo para o armazenamento do app; Cancelar o apaga
data class FotoPreparada(
    val caminhoTemporario: String = "",
    val nomeArquivo: String = "",
    val tamanhoBytes: Long = 0
)
