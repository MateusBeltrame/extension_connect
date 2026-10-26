package br.com.mateushb.extensionconnectapp.model

// Resultado de "Salvar alterações" em Editar pessoa, com as regras conferidas no banco (spec 007)
sealed interface ResultadoSalvarPessoa {
    data class Sucesso(val usuario: Usuario) : ResultadoSalvarPessoa
    data class Bloqueado(val bloqueio: BloqueioEdicaoPessoa) : ResultadoSalvarPessoa
    data object NaoEncontrada : ResultadoSalvarPessoa

    // Administrador tentou mudar o próprio perfil ou se desativar (HU-05)
    data object EdicaoPropriaProibida : ResultadoSalvarPessoa
}
