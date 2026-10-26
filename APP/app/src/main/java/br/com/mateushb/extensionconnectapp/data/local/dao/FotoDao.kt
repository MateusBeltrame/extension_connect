package br.com.mateushb.extensionconnectapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import br.com.mateushb.extensionconnectapp.data.local.entity.FotoComAutor
import br.com.mateushb.extensionconnectapp.data.local.entity.FotoEntity
import kotlinx.coroutines.flow.Flow

// Fotos do projeto (spec 006): mesmas regras do ArquivoDao, mais a autorização de divulgação no Mural
@Dao
interface FotoDao {

    @Transaction
    @Query("SELECT * FROM fotos WHERE projeto_id = :projetoId ORDER BY id DESC")
    fun observarFotosPorProjeto(projetoId: Int): Flow<List<FotoComAutor>>

    // Conta ativa, projeto não finalizado e participante ou orientador (spec 006, Permissões)
    @Query(RegrasPermissao.PODE_ENVIAR_ARQUIVOS)
    suspend fun contarPodeEnviar(projetoId: Int, autorId: Int): Int

    @Insert
    suspend fun inserir(foto: FotoEntity): Long

    // Confere a permissão na mesma transação da gravação; null = nada gravado
    @Transaction
    suspend fun enfileirar(foto: FotoEntity): Long? {
        if (contarPodeEnviar(foto.projetoId, foto.autorId) == 0) return null
        return inserir(foto)
    }

    @Query("SELECT caminho_local FROM fotos WHERE id = :id")
    suspend fun buscarCaminho(id: Int): String?

    // Só a própria foto, de conta ativa e em projeto não finalizado; retorna quantas linhas mudou
    @Query(
        "UPDATE fotos SET autorizada_mural = :autorizada WHERE id = :id AND autor_id = :autorId " +
            "AND ${RegrasPermissao.PROJETO_NAO_FINALIZADO} AND ${RegrasPermissao.AUTOR_ATIVO}"
    )
    suspend fun alterarAutorizacaoDoAutor(id: Int, autorId: Int, autorizada: Boolean): Int

    @Query(
        "DELETE FROM fotos WHERE id = :id AND autor_id = :autorId " +
            "AND ${RegrasPermissao.PROJETO_NAO_FINALIZADO} AND ${RegrasPermissao.AUTOR_ATIVO}"
    )
    suspend fun excluirDoAutor(id: Int, autorId: Int): Int

    // ===== Fila de envio =====

    @Query("SELECT * FROM fotos WHERE status_envio != 'ENVIADO' ORDER BY id ASC LIMIT 1")
    suspend fun buscarProximoPendente(): FotoEntity?

    @Query("UPDATE fotos SET status_envio = :status, progresso = :progresso WHERE id = :id AND status_envio != 'ENVIADO'")
    suspend fun atualizarEnvio(id: Int, status: String, progresso: Int?): Int

    @Query("UPDATE fotos SET status_envio = :para, progresso = :progresso WHERE status_envio = :de")
    suspend fun marcarPendentes(de: String, para: String, progresso: Int?)

    @Query("SELECT COUNT(*) FROM fotos WHERE status_envio != 'ENVIADO'")
    fun observarQuantidadePendentes(): Flow<Int>
}
