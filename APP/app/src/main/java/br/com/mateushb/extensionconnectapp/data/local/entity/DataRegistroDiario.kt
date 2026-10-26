package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo

// Resultado de consulta (não é @Entity): só o projeto e a data de cada registro do diário, para a
// regra "parado" da lista de Projetos (spec 005) sem carregar os textos nem pesar nos detalhes
data class DataRegistroDiario(
    @ColumnInfo(name = "projeto_id")
    val projetoId: Int,

    @ColumnInfo(name = "data")
    val data: String
)
