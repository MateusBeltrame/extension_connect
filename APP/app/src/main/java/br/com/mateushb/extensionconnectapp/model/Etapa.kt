package br.com.mateushb.extensionconnectapp.model

data class Etapa(
    val id: Int = 0,
    val projetoId: Int = 0,
    val nome: String = "",
    val dataPrevista: String = "",
    val concluida: Boolean = false,
    val ordem: Int = 0
)
