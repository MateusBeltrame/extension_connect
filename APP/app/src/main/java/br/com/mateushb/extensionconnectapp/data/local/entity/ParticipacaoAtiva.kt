package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo

// Resultado de consulta (não é @Entity): aluno que já está num projeto não finalizado e o grupo
// desse projeto. Bloqueia o aluno na busca do wizard "Novo projeto" (spec 003)
data class ParticipacaoAtiva(
    @ColumnInfo(name = "aluno_id")
    val alunoId: Int,

    @ColumnInfo(name = "nome_grupo")
    val nomeGrupo: String
)
