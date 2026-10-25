package br.com.mateushb.extensionconnectapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import br.com.mateushb.extensionconnectapp.data.local.entity.ProjetoComRelacoes
import kotlinx.coroutines.flow.Flow

// Só leitura nesta versão: criar/editar projeto entra na feature 3 (Novo projeto)
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
}
