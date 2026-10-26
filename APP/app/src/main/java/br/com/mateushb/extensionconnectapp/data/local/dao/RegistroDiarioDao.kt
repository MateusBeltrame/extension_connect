package br.com.mateushb.extensionconnectapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import br.com.mateushb.extensionconnectapp.data.local.entity.RegistroDiarioComAutor
import br.com.mateushb.extensionconnectapp.data.local.entity.RegistroDiarioEntity
import kotlinx.coroutines.flow.Flow

// Diário do projeto (spec 005): só inclusão e exclusão do próprio registro, sem edição. O filtro por
// tipo roda em memória sobre a lista do projeto (07-dados.md)
@Dao
interface RegistroDiarioDao {

    // id DESC só como base estável: o Repository reordena pelo Instant de data
    @Transaction
    @Query("SELECT * FROM registros_diario WHERE projeto_id = :projetoId ORDER BY id DESC")
    fun observarRegistrosPorProjeto(projetoId: Int): Flow<List<RegistroDiarioComAutor>>

    // Conta ativa, projeto não finalizado e o papel bate com o tipo: orientador registra Orientação,
    // participante registra Atividade. Desativar a conta tira o direito mesmo com a sessão aberta
    @Query(
        "SELECT COUNT(*) FROM projetos p WHERE p.id = :projetoId AND p.estagio != 'FINALIZADO' " +
            "AND EXISTS (SELECT 1 FROM usuarios u WHERE u.id = :autorId AND u.ativo = 1) AND (" +
            "(:tipo = 'ORIENTACAO' AND p.orientador_id = :autorId) OR " +
            "(:tipo = 'ATIVIDADE' AND EXISTS (SELECT 1 FROM projeto_participantes pp " +
            "WHERE pp.projeto_id = p.id AND pp.aluno_id = :autorId)))"
    )
    suspend fun contarPodePublicar(projetoId: Int, autorId: Int, tipo: String): Int

    @Query("SELECT COUNT(*) FROM etapas WHERE id = :etapaId AND projeto_id = :projetoId")
    suspend fun contarEtapaDoProjeto(etapaId: Int, projetoId: Int): Int

    @Insert
    suspend fun inserir(registro: RegistroDiarioEntity): Long

    // Confere a permissão na mesma transação da gravação: se o projeto foi finalizado ou o usuário
    // saiu do projeto desde que a tela leu, não grava nada. Etapa de outro projeto vira "sem etapa"
    @Transaction
    suspend fun publicar(registro: RegistroDiarioEntity): Boolean {
        if (contarPodePublicar(registro.projetoId, registro.autorId, registro.tipo) == 0) return false
        val etapaValida = registro.etapaId?.takeIf { contarEtapaDoProjeto(it, registro.projetoId) > 0 }
        inserir(registro.copy(etapaId = etapaValida))
        return true
    }

    // Só o próprio registro, de conta ativa e em projeto não finalizado; retorna quantas linhas apagou (0 ou 1)
    @Query(
        "DELETE FROM registros_diario WHERE id = :id AND autor_id = :autorId " +
            "AND projeto_id IN (SELECT id FROM projetos WHERE estagio != 'FINALIZADO') " +
            "AND EXISTS (SELECT 1 FROM usuarios u WHERE u.id = :autorId AND u.ativo = 1)"
    )
    suspend fun excluirDoAutor(id: Int, autorId: Int): Int
}
