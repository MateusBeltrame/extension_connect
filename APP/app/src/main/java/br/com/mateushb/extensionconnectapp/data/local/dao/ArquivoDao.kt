package br.com.mateushb.extensionconnectapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import br.com.mateushb.extensionconnectapp.data.local.entity.ArquivoComAutor
import br.com.mateushb.extensionconnectapp.data.local.entity.ArquivoEntity
import kotlinx.coroutines.flow.Flow

// Documentos do projeto (spec 006): inclusão pela fila de envio e exclusão do próprio documento, sem
// edição. As consultas "da fila" são usadas só pela FilaEnvio
@Dao
interface ArquivoDao {

    // Todos os status: a aba separa enviados e pendentes. A ordem de exibição é feita em memória (Instant)
    @Transaction
    @Query("SELECT * FROM arquivos WHERE projeto_id = :projetoId ORDER BY id DESC")
    fun observarArquivosPorProjeto(projetoId: Int): Flow<List<ArquivoComAutor>>

    // Conta ativa, projeto não finalizado e participante ou orientador (spec 006, Permissões)
    @Query(RegrasPermissao.PODE_ENVIAR_ARQUIVOS)
    suspend fun contarPodeEnviar(projetoId: Int, autorId: Int): Int

    @Insert
    suspend fun inserir(arquivo: ArquivoEntity): Long

    // Confere a permissão na mesma transação da gravação; null = nada gravado
    @Transaction
    suspend fun enfileirar(arquivo: ArquivoEntity): Long? {
        if (contarPodeEnviar(arquivo.projetoId, arquivo.autorId) == 0) return null
        return inserir(arquivo)
    }

    @Query("SELECT caminho_local FROM arquivos WHERE id = :id")
    suspend fun buscarCaminho(id: Int): String?

    // Só o próprio documento, de conta ativa e em projeto não finalizado; retorna quantas linhas apagou
    @Query(
        "DELETE FROM arquivos WHERE id = :id AND autor_id = :autorId " +
            "AND ${RegrasPermissao.PROJETO_NAO_FINALIZADO} AND ${RegrasPermissao.AUTOR_ATIVO}"
    )
    suspend fun excluirDoAutor(id: Int, autorId: Int): Int

    // ===== Fila de envio =====

    // O mais antigo da fila (ordem de gravação)
    @Query("SELECT * FROM arquivos WHERE status_envio != 'ENVIADO' ORDER BY id ASC LIMIT 1")
    suspend fun buscarProximoPendente(): ArquivoEntity?

    // Não volta um item já enviado; 0 linhas = o item não existe mais (projeto excluído)
    @Query("UPDATE arquivos SET status_envio = :status, progresso = :progresso WHERE id = :id AND status_envio != 'ENVIADO'")
    suspend fun atualizarEnvio(id: Int, status: String, progresso: Int?): Int

    // Todos os pendentes de um status para outro (internet caiu ou voltou, app reaberto)
    @Query("UPDATE arquivos SET status_envio = :para, progresso = :progresso WHERE status_envio = :de")
    suspend fun marcarPendentes(de: String, para: String, progresso: Int?)

    @Query("SELECT COUNT(*) FROM arquivos WHERE status_envio != 'ENVIADO'")
    fun observarQuantidadePendentes(): Flow<Int>
}
