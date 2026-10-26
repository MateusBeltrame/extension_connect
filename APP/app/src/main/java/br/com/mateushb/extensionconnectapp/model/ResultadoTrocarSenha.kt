package br.com.mateushb.extensionconnectapp.model

// Resultado de "Salvar nova senha" (spec 008): a senha atual é conferida no banco antes de gravar
sealed interface ResultadoTrocarSenha {
    data object Sucesso : ResultadoTrocarSenha
    data object SenhaAtualIncorreta : ResultadoTrocarSenha
    data object NaoEncontrado : ResultadoTrocarSenha
}
