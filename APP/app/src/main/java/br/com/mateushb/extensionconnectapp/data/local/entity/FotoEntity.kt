package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.mateushb.extensionconnectapp.model.Foto
import br.com.mateushb.extensionconnectapp.model.StatusEnvio

// Foto do projeto (spec 006). Excluir o projeto apaga as fotos; o autor nunca é excluído (só desativado)
@Entity(
    tableName = "fotos",
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
data class FotoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "projeto_id")
    val projetoId: Int,

    @ColumnInfo(name = "autor_id")
    val autorId: Int,

    @ColumnInfo(name = "nome_arquivo")
    val nomeArquivo: String,

    @ColumnInfo(name = "tamanho_bytes")
    val tamanhoBytes: Long,

    @ColumnInfo(name = "caminho_local")
    val caminhoLocal: String,

    // Instant ISO (Instant.now().toString())
    @ColumnInfo(name = "data_envio")
    val dataEnvio: String,

    @ColumnInfo(name = "legenda")
    val legenda: String? = null,

    @ColumnInfo(name = "autorizada_mural")
    val autorizadaMural: Boolean,

    @ColumnInfo(name = "status_envio")
    val statusEnvio: String,

    @ColumnInfo(name = "progresso")
    val progresso: Int? = null
) {
    fun toFoto(): Foto = Foto(
        id = id,
        projetoId = projetoId,
        autorId = autorId,
        nomeArquivo = nomeArquivo,
        tamanhoBytes = tamanhoBytes,
        caminhoLocal = caminhoLocal,
        dataEnvio = dataEnvio,
        legenda = legenda,
        autorizadaMural = autorizadaMural,
        statusEnvio = StatusEnvio.entries.firstOrNull { it.name == statusEnvio } ?: StatusEnvio.AGUARDANDO_ENVIO,
        progresso = progresso
    )

    companion object {
        fun fromFoto(foto: Foto): FotoEntity = FotoEntity(
            id = foto.id,
            projetoId = foto.projetoId,
            autorId = foto.autorId,
            nomeArquivo = foto.nomeArquivo,
            tamanhoBytes = foto.tamanhoBytes,
            caminhoLocal = foto.caminhoLocal,
            dataEnvio = foto.dataEnvio,
            legenda = foto.legenda,
            autorizadaMural = foto.autorizadaMural,
            statusEnvio = foto.statusEnvio.name,
            progresso = foto.progresso
        )
    }
}
