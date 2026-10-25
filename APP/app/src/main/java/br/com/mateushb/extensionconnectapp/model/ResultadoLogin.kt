package br.com.mateushb.extensionconnectapp.model

sealed interface ResultadoLogin {
    data class Sucesso(val usuario: Usuario) : ResultadoLogin
    data object CredenciaisInvalidas : ResultadoLogin
    data object ContaDesativada : ResultadoLogin
}
