package br.com.mateushb.extensionconnectapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.mateushb.extensionconnectapp.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Int): UsuarioEntity?

    // Perfil gravado pelo name (PerfilUsuario); busca do wizard "Novo projeto" (orientador e alunos)
    @Query("SELECT * FROM usuarios WHERE perfil = :perfil AND ativo = 1")
    fun observarAtivosPorPerfil(perfil: String): Flow<List<UsuarioEntity>>

    // ABORT: e-mail repetido viola o índice único em vez de sobrescrever a conta
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(usuario: UsuarioEntity): Long

    @Update
    suspend fun atualizar(usuario: UsuarioEntity)
}
