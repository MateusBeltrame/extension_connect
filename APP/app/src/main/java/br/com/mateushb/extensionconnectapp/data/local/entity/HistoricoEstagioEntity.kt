package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.HistoricoEstagio

@Entity(
    tableName = "historico_estagios",
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
            childColumns = ["autor_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["projeto_id"]),
        Index(value = ["autor_id"])
    ]
)
data class HistoricoEstagioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "projeto_id")
    val projetoId: Int,

    @ColumnInfo(name = "estagio")
    val estagio: String,

    @ColumnInfo(name = "autor_id")
    val autorId: Int,

    // Instant ISO (Instant.now().toString())
    @ColumnInfo(name = "data")
    val data: String,

    @ColumnInfo(name = "justificativa")
    val justificativa: String? = null
) {
    fun toHistoricoEstagio(): HistoricoEstagio = HistoricoEstagio(
        id = id,
        projetoId = projetoId,
        estagio = EstagioProjeto.entries.firstOrNull { it.name == estagio } ?: EstagioProjeto.EM_ANALISE,
        autorId = autorId,
        data = data,
        justificativa = justificativa
    )

    companion object {
        fun fromHistoricoEstagio(historico: HistoricoEstagio): HistoricoEstagioEntity = HistoricoEstagioEntity(
            id = historico.id,
            projetoId = historico.projetoId,
            estagio = historico.estagio.name,
            autorId = historico.autorId,
            data = historico.data,
            justificativa = historico.justificativa
        )
    }
}
