package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.mateushb.extensionconnectapp.model.Arquivo
import br.com.mateushb.extensionconnectapp.model.FormatoDocumento
import br.com.mateushb.extensionconnectapp.model.StatusEnvio

// Documento do projeto (spec 006). Excluir o projeto apaga os documentos; o autor nunca é excluído
// (só desativado)
@Entity(
    tableName = "arquivos",
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
data class ArquivoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "projeto_id")
    val projetoId: Int,

    @ColumnInfo(name = "autor_id")
    val autorId: Int,

    @ColumnInfo(name = "nome_arquivo")
    val nomeArquivo: String,

    // FormatoDocumento.name (PDF, DOCX, XLSX, TXT)
    @ColumnInfo(name = "extensao")
    val extensao: String,

    @ColumnInfo(name = "tamanho_bytes")
    val tamanhoBytes: Long,

    @ColumnInfo(name = "caminho_local")
    val caminhoLocal: String,

    // Instant ISO (Instant.now().toString())
    @ColumnInfo(name = "data_envio")
    val dataEnvio: String,

    @ColumnInfo(name = "status_envio")
    val statusEnvio: String,

    @ColumnInfo(name = "progresso")
    val progresso: Int? = null
) {
    fun toArquivo(): Arquivo = Arquivo(
        id = id,
        projetoId = projetoId,
        autorId = autorId,
        nomeArquivo = nomeArquivo,
        extensao = FormatoDocumento.entries.firstOrNull { it.name == extensao } ?: FormatoDocumento.PDF,
        tamanhoBytes = tamanhoBytes,
        caminhoLocal = caminhoLocal,
        dataEnvio = dataEnvio,
        statusEnvio = StatusEnvio.entries.firstOrNull { it.name == statusEnvio } ?: StatusEnvio.AGUARDANDO_ENVIO,
        progresso = progresso
    )

    companion object {
        fun fromArquivo(arquivo: Arquivo): ArquivoEntity = ArquivoEntity(
            id = arquivo.id,
            projetoId = arquivo.projetoId,
            autorId = arquivo.autorId,
            nomeArquivo = arquivo.nomeArquivo,
            extensao = arquivo.extensao.name,
            tamanhoBytes = arquivo.tamanhoBytes,
            caminhoLocal = arquivo.caminhoLocal,
            dataEnvio = arquivo.dataEnvio,
            statusEnvio = arquivo.statusEnvio.name,
            progresso = arquivo.progresso
        )
    }
}
