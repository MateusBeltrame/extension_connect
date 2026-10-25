package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.Usuario
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

    @Relation(
        parentColumn = "id",
        entityColumn = "projeto_id"
    )
    val historico: List<HistoricoEstagioEntity> = emptyList()
) {
    fun toProjetoResumo(): ProjetoResumo = ProjetoResumo(
        projeto = projeto.toProjeto(),
        // FK RESTRICT garante o orientador; o fallback só evita derrubar a lista se o banco estiver inconsistente
        orientador = orientador?.let { semSenha(it) } ?: Usuario(id = projeto.orientadorId),
        participantes = participantes.map { semSenha(it) }.sortedWith(compareBy(COLLATOR) { it.nome }),
        etapas = etapas.sortedBy { it.ordem }.map { it.toEtapa() },
        // Compara como Instant: o texto ISO muda de tamanho com a fração de segundo e não ordena certo
        dataUltimaMudancaEstagio = historico.maxByOrNull { instanteOuMinimo(it.data) }?.data
    )

    companion object {
        private val COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

        // O hash de senha não sai da camada de dados: o estado de tela pode acabar em log/relatório de crash
        private fun semSenha(entity: UsuarioEntity): Usuario = entity.toUsuario().copy(senhaHash = "")

        private fun instanteOuMinimo(iso: String): Instant =
            try {
                Instant.parse(iso)
            } catch (_: Exception) {
                Instant.MIN
            }
    }
}
