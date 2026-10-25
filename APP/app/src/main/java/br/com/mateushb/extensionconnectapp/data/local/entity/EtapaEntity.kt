package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.mateushb.extensionconnectapp.model.Etapa

@Entity(
    tableName = "etapas",
    foreignKeys = [
        ForeignKey(
            entity = ProjetoEntity::class,
            parentColumns = ["id"],
            childColumns = ["projeto_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projeto_id"])]
)
data class EtapaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "projeto_id")
    val projetoId: Int,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "data_prevista")
    val dataPrevista: String,

    @ColumnInfo(name = "concluida")
    val concluida: Boolean,

    @ColumnInfo(name = "ordem")
    val ordem: Int
) {
    fun toEtapa(): Etapa = Etapa(
        id = id,
        projetoId = projetoId,
        nome = nome,
        dataPrevista = dataPrevista,
        concluida = concluida,
        ordem = ordem
    )

    companion object {
        fun fromEtapa(etapa: Etapa): EtapaEntity = EtapaEntity(
            id = etapa.id,
            projetoId = etapa.projetoId,
            nome = etapa.nome,
            dataPrevista = etapa.dataPrevista,
            concluida = etapa.concluida,
            ordem = etapa.ordem
        )
    }
}
