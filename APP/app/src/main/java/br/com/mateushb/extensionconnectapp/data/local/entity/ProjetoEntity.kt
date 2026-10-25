package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.model.TemaProjeto

// RESTRICT: usuário nunca é excluído, só desativado (05-dominio.md)
@Entity(
    tableName = "projetos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["orientador_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["orientador_id"])]
)
data class ProjetoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "nome_grupo")
    val nomeGrupo: String,

    @ColumnInfo(name = "descricao")
    val descricao: String,

    // Enums gravados pelo name
    @ColumnInfo(name = "tema")
    val tema: String,

    @ColumnInfo(name = "orientador_id")
    val orientadorId: Int,

    @ColumnInfo(name = "data_inicio")
    val dataInicio: String,

    @ColumnInfo(name = "data_fim_previsto")
    val dataFimPrevisto: String,

    @ColumnInfo(name = "estagio")
    val estagio: String,

    @ColumnInfo(name = "aplicacao_inicio")
    val aplicacaoInicio: String? = null,

    @ColumnInfo(name = "aplicacao_fim")
    val aplicacaoFim: String? = null,

    @ColumnInfo(name = "aplicacao_descricao")
    val aplicacaoDescricao: String? = null,

    @ColumnInfo(name = "aguardando_ciencia_orientador")
    val aguardandoCienciaOrientador: Boolean
) {
    fun toProjeto(): Projeto = Projeto(
        id = id,
        titulo = titulo,
        nomeGrupo = nomeGrupo,
        descricao = descricao,
        tema = TemaProjeto.entries.firstOrNull { it.name == tema } ?: TemaProjeto.EDUCACAO,
        orientadorId = orientadorId,
        dataInicio = dataInicio,
        dataFimPrevisto = dataFimPrevisto,
        estagio = EstagioProjeto.entries.firstOrNull { it.name == estagio } ?: EstagioProjeto.EM_ANALISE,
        aplicacaoInicio = aplicacaoInicio,
        aplicacaoFim = aplicacaoFim,
        aplicacaoDescricao = aplicacaoDescricao,
        aguardandoCienciaOrientador = aguardandoCienciaOrientador
    )

    companion object {
        fun fromProjeto(projeto: Projeto): ProjetoEntity = ProjetoEntity(
            id = projeto.id,
            titulo = projeto.titulo,
            nomeGrupo = projeto.nomeGrupo,
            descricao = projeto.descricao,
            tema = projeto.tema.name,
            orientadorId = projeto.orientadorId,
            dataInicio = projeto.dataInicio,
            dataFimPrevisto = projeto.dataFimPrevisto,
            estagio = projeto.estagio.name,
            aplicacaoInicio = projeto.aplicacaoInicio,
            aplicacaoFim = projeto.aplicacaoFim,
            aplicacaoDescricao = projeto.aplicacaoDescricao,
            aguardandoCienciaOrientador = projeto.aguardandoCienciaOrientador
        )
    }
}
