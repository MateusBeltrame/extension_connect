package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

// Linha do histórico de estágio + quem a gravou, para o card "Histórico de estágios" (spec 004)
data class HistoricoComAutor(
    @Embedded
    val historico: HistoricoEstagioEntity,

    @Relation(
        parentColumn = "autor_id",
        entityColumn = "id"
    )
    val autor: UsuarioEntity?
)
