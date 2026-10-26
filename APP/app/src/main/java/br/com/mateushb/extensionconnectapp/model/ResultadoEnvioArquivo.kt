package br.com.mateushb.extensionconnectapp.model

// A permissão de envio é conferida no banco na mesma transação da gravação (spec 006, HU-09)
sealed interface ResultadoEnvioArquivo {
    data object Sucesso : ResultadoEnvioArquivo
    data object SemPermissao : ResultadoEnvioArquivo
}
