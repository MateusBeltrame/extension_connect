package br.com.mateushb.extensionconnectapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import br.com.mateushb.extensionconnectapp.data.local.ConflitoNovoProjeto
import br.com.mateushb.extensionconnectapp.data.local.entity.DataRegistroDiario
import br.com.mateushb.extensionconnectapp.data.local.entity.EtapaEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.HistoricoEstagioEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.ParticipacaoAtiva
import br.com.mateushb.extensionconnectapp.data.local.entity.ProjetoComRelacoes
import br.com.mateushb.extensionconnectapp.data.local.entity.ProjetoEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.ProjetoParticipanteEntity
import kotlinx.coroutines.flow.Flow

// Criar projeto entrou na feature 3 (Novo projeto); mudar estágio, ciência e etapa concluída na feature 4.
// Editar projeto/etapas (e um EtapaDao próprio) fica para a feature de edição
@Dao
interface ProjetoDao {

    @Transaction
    @Query("SELECT * FROM projetos")
    fun observarProjetosCompletos(): Flow<List<ProjetoComRelacoes>>

    @Transaction
    @Query(
        "SELECT * FROM projetos WHERE id IN " +
            "(SELECT projeto_id FROM projeto_participantes WHERE aluno_id = :alunoId)"
    )
    fun observarProjetosCompletosDoAluno(alunoId: Int): Flow<List<ProjetoComRelacoes>>

    @Transaction
    @Query("SELECT * FROM projetos WHERE id = :id")
    fun observarProjetoCompletoPorId(id: Int): Flow<ProjetoComRelacoes?>

    // Datas do diário para a regra "parado" (spec 005). Sem MAX no SQL: o texto ISO muda de tamanho
    // com a fração de segundo e não ordena certo; o Repository compara como Instant
    @Query("SELECT projeto_id, data FROM registros_diario")
    fun observarDatasDiario(): Flow<List<DataRegistroDiario>>

    // Aluno só pode estar em um projeto não finalizado por vez (05-dominio.md)
    @Query(
        "SELECT pp.aluno_id, p.nome_grupo FROM projeto_participantes pp " +
            "INNER JOIN projetos p ON p.id = pp.projeto_id WHERE p.estagio != 'FINALIZADO'"
    )
    fun observarParticipacoesAtivas(): Flow<List<ParticipacaoAtiva>>

    @Query(
        "SELECT pp.aluno_id, p.nome_grupo FROM projeto_participantes pp " +
            "INNER JOIN projetos p ON p.id = pp.projeto_id " +
            "WHERE p.estagio != 'FINALIZADO' AND pp.aluno_id IN (:alunoIds)"
    )
    suspend fun buscarParticipacoesAtivas(alunoIds: List<Int>): List<ParticipacaoAtiva>

    @Query("SELECT COUNT(*) FROM usuarios WHERE id = :id AND ativo = 1 AND perfil = 'PROFESSOR'")
    suspend fun contarOrientadorAtivo(id: Int): Int

    // Participante escolhido que deixou de ser aluno ativo (desativado ou de outro perfil)
    @Query("SELECT id FROM usuarios WHERE id IN (:alunoIds) AND (ativo = 0 OR perfil != 'ALUNO')")
    suspend fun buscarAlunosIndisponiveis(alunoIds: List<Int>): List<Int>

    @Insert
    suspend fun inserirProjeto(projeto: ProjetoEntity): Long

    @Insert
    suspend fun inserirEtapas(etapas: List<EtapaEntity>)

    @Insert
    suspend fun inserirParticipantes(participantes: List<ProjetoParticipanteEntity>)

    @Insert
    suspend fun inserirHistorico(historico: HistoricoEstagioEntity)

    // Projeto, cronograma, participantes e a linha "Projeto criado" do histórico: grava tudo ou nada.
    // As conferências rodam na mesma transação da gravação; um ConflitoNovoProjeto a desfaz
    @Transaction
    suspend fun inserirProjetoCompleto(
        projeto: ProjetoEntity,
        etapas: List<EtapaEntity>,
        alunoIds: List<Int>,
        historico: HistoricoEstagioEntity
    ): Int {
        if (contarOrientadorAtivo(projeto.orientadorId) == 0) throw ConflitoNovoProjeto.OrientadorInativo()
        buscarAlunosIndisponiveis(alunoIds).firstOrNull()?.let { throw ConflitoNovoProjeto.AlunoInativo(it) }
        buscarParticipacoesAtivas(alunoIds).firstOrNull()?.let {
            throw ConflitoNovoProjeto.AlunoOcupado(it.alunoId, it.nomeGrupo)
        }

        val projetoId = inserirProjeto(projeto).toInt()
        inserirEtapas(etapas.map { it.copy(projetoId = projetoId) })
        inserirParticipantes(alunoIds.map { ProjetoParticipanteEntity(projetoId = projetoId, alunoId = it) })
        inserirHistorico(historico.copy(projetoId = projetoId))
        return projetoId
    }

    // ===== Ciclo de estágio e cronograma (spec 004) =====

    @Query("SELECT estagio FROM projetos WHERE id = :id")
    suspend fun buscarEstagio(id: Int): String?

    @Query("UPDATE projetos SET estagio = :estagio, aguardando_ciencia_orientador = :aguardandoCiencia WHERE id = :id")
    suspend fun atualizarEstagio(id: Int, estagio: String, aguardandoCiencia: Boolean)

    @Query(
        "UPDATE projetos SET aplicacao_inicio = :inicio, aplicacao_fim = :fim, " +
            "aplicacao_descricao = :descricao WHERE id = :id"
    )
    suspend fun atualizarAplicacao(id: Int, inicio: String, fim: String, descricao: String)

    // Confere o estágio de origem na mesma transação: se mudou desde que a tela leu (outro aparelho,
    // toque duplo), não grava nada. Aplicação nula mantém a atual (ao voltar, ela reaparece no 7a)
    @Transaction
    suspend fun mudarEstagio(
        projetoId: Int,
        origem: String,
        destino: String,
        aguardandoCiencia: Boolean,
        aplicacaoInicio: String?,
        aplicacaoFim: String?,
        aplicacaoDescricao: String?,
        historico: HistoricoEstagioEntity
    ): Boolean {
        if (buscarEstagio(projetoId) != origem) return false
        if (aplicacaoInicio != null && aplicacaoFim != null && aplicacaoDescricao != null) {
            atualizarAplicacao(projetoId, aplicacaoInicio, aplicacaoFim, aplicacaoDescricao)
        }
        atualizarEstagio(projetoId, destino, aguardandoCiencia)
        inserirHistorico(historico)
        return true
    }

    @Query("UPDATE projetos SET aguardando_ciencia_orientador = 0 WHERE id = :id")
    suspend fun confirmarCiencia(id: Int)

    // Inverte no próprio banco: dois toques rápidos alternam duas vezes, em vez de gravar o mesmo valor
    // lido de um estado de tela ainda desatualizado (revisão da spec 004)
    @Query("UPDATE etapas SET concluida = NOT concluida WHERE id = :etapaId")
    suspend fun alternarConclusaoEtapa(etapaId: Int)
}
