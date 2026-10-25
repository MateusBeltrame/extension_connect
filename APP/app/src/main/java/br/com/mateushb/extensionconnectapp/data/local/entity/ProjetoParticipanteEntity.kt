package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Vínculo N:N projeto ↔ aluno: remover a linha não afeta o cadastro do aluno
@Entity(
    tableName = "projeto_participantes",
    primaryKeys = ["projeto_id", "aluno_id"],
    foreignKeys = [
        ForeignKey(
            entity = ProjetoEntity::class,
            parentColumns = ["id"],
            childColumns = ["projeto_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["aluno_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projeto_id"]),
        Index(value = ["aluno_id"])
    ]
)
data class ProjetoParticipanteEntity(
    @ColumnInfo(name = "projeto_id")
    val projetoId: Int,

    @ColumnInfo(name = "aluno_id")
    val alunoId: Int
)
