package br.com.mateushb.extensionconnectapp.model

// A permissão é conferida no banco na mesma transação da gravação (spec 005, HU-03)
sealed interface ResultadoPublicarRegistro {
    data object Sucesso : ResultadoPublicarRegistro
    data object SemPermissao : ResultadoPublicarRegistro
}
