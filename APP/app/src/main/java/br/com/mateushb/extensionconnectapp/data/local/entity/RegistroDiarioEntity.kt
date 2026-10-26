package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.mateushb.extensionconnectapp.model.RegistroDiario
import br.com.mateushb.extensionconnectapp.model.TipoRegistro

// Diário do projeto (spec 005). Excluir o projeto apaga o diário; o autor nunca é excluído (só
// desativado); excluir a etapa só desfaz o vínculo
@Entity(
    tableName = "registros_diario",
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
        ),
        ForeignKey(
            entity = EtapaEntity::class,
            parentColumns = ["id"],
            childColumns = ["etapa_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["projeto_id"]),
        Index(value = ["autor_id"]),
        Index(value = ["etapa_id"])
    ]
)
data class RegistroDiarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "projeto_id")
    val projetoId: Int,

    @ColumnInfo(name = "autor_id")
    val autorId: Int,

    // Instant ISO (Instant.now().toString())
    @ColumnInfo(name = "data")
    val data: String,

    @ColumnInfo(name = "tipo")
    val tipo: String,

    @ColumnInfo(name = "etapa_id")
    val etapaId: Int? = null,

    @ColumnInfo(name = "texto")
    val texto: String
) {
    fun toRegistroDiario(): RegistroDiario = RegistroDiario(
        id = id,
        projetoId = projetoId,
        autorId = autorId,
        data = data,
        tipo = TipoRegistro.entries.firstOrNull { it.name == tipo } ?: TipoRegistro.ATIVIDADE,
        etapaId = etapaId,
        texto = texto
    )

    companion object {
        fun fromRegistroDiario(registro: RegistroDiario): RegistroDiarioEntity = RegistroDiarioEntity(
            id = registro.id,
            projetoId = registro.projetoId,
            autorId = registro.autorId,
            data = registro.data,
            tipo = registro.tipo.name,
            etapaId = registro.etapaId,
            texto = registro.texto
        )
    }
}
