package br.com.mateushb.extensionconnectapp.model

// O estágio é conferido no banco na mesma transação da gravação (spec 004, HU-07)
sealed interface ResultadoMudancaEstagio {
    data object Sucesso : ResultadoMudancaEstagio
    data object EstagioMudou : ResultadoMudancaEstagio
}
