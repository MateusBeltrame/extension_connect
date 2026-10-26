package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import br.com.mateushb.extensionconnectapp.model.EventoHistorico
import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.model.lerInstanteIso
import java.text.Collator
import java.time.Instant
import java.util.Locale

// Projeto + orientador + participantes + etapas + histórico, lido de uma vez pelo ProjetoDao
data class ProjetoComRelacoes(
    @Embedded
    val projeto: ProjetoEntity,

    @Relation(
        parentColumn = "orientador_id",
        entityColumn = "id"
    )
    val orientador: UsuarioEntity?,

    @Relation(
        parentColumn = "id",
        entity = UsuarioEntity::class,
        entityColumn = "id",
        associateBy = Junction(
            value = ProjetoParticipanteEntity::class,
            parentColumn = "projeto_id",
            entityColumn = "aluno_id"
        )
    )
    val participantes: List<UsuarioEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "projeto_id"
    )
    val etapas: List<EtapaEntity> = emptyList(),

    // Com o autor de cada linha: os detalhes leem tudo numa única consulta, sem combinar dois Flows
    // que podem emitir fora de sincronia depois de uma mudança de estágio (revisão da spec 004)
    @Relation(
        parentColumn = "id",
        entity = HistoricoEstagioEntity::class,
        entityColumn = "projeto_id"
    )
    val historico: List<HistoricoComAutor> = emptyList()
) {
    // O último registro do diário (regra "parado", spec 005) vem de uma consulta à parte, só na lista:
    // como @Relation, os detalhes também observariam registros_diario e recompunham a cada publicação
    fun toProjetoResumo(dataUltimoRegistroDiario: String? = null): ProjetoResumo = ProjetoResumo(
        projeto = projeto.toProjeto(),
        // FK RESTRICT garante o orientador; o fallback só evita derrubar a lista se o banco estiver inconsistente
        orientador = orientador?.let { semSenha(it) } ?: Usuario(id = projeto.orientadorId),
        participantes = participantes.map { semSenha(it) }.sortedWith(compareBy(COLLATOR) { it.nome }),
        etapas = etapas.sortedBy { it.ordem }.map { it.toEtapa() },
        // Compara como Instant: o texto ISO muda de tamanho com a fração de segundo e não ordena certo
        dataUltimaMudancaEstagio = historico.maxByOrNull { lerInstanteIso(it.historico.data) ?: Instant.MIN }?.historico?.data,
        dataUltimoRegistroDiario = dataUltimoRegistroDiario
    )

    // Histórico do mais recente para o mais antigo, ordenado pelo id (autoincremento = ordem de
    // gravação, imune a relógio do aparelho ajustado para trás). O estágio anterior de cada linha é o
    // da linha gravada logo antes; a mais antiga é a criação do projeto
    fun toProjetoDetalhes(): ProjetoDetalhes {
        val cronologico = historico.sortedBy { it.historico.id }
        val eventos = cronologico.mapIndexed { indice, linha ->
            EventoHistorico(
                historico = linha.historico.toHistoricoEstagio(),
                autor = linha.autor?.let { semSenha(it) },
                estagioAnterior = cronologico.getOrNull(indice - 1)?.historico?.toHistoricoEstagio()?.estagio
            )
        }
        return ProjetoDetalhes(resumo = toProjetoResumo(), historico = eventos.reversed())
    }

    companion object {
        private val COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

        // O hash de senha não sai da camada de dados: o estado de tela pode acabar em log/relatório de crash
        private fun semSenha(entity: UsuarioEntity): Usuario = entity.toUsuario().copy(senhaHash = "")

    }
}
