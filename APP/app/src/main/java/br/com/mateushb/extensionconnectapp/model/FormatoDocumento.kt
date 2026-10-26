package br.com.mateushb.extensionconnectapp.model

// Formatos de documento aceitos na aba Arquivos (spec 006, frame 35:408). O nome do enum é a extensão
// exibida no selo e gravada na coluna extensao
enum class FormatoDocumento(val mime: String) {
    PDF("application/pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    TXT("text/plain");

    companion object {
        // Pela extensão do nome, sem diferenciar maiúsculas; null = formato não aceito
        fun doNome(nomeArquivo: String): FormatoDocumento? {
            val extensao = nomeArquivo.substringAfterLast('.', missingDelimiterValue = "")
            return entries.firstOrNull { it.name.equals(extensao, ignoreCase = true) }
        }
    }
}
