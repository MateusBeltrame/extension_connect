package br.com.mateushb.extensionconnectapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.com.mateushb.extensionconnectapp.data.local.ConflitoEdicaoPessoa
import br.com.mateushb.extensionconnectapp.data.local.entity.UsuarioEntity
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.bloqueioEdicaoPessoa
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Int): UsuarioEntity?

    // Sessão: o usuário logado acompanha a própria linha (nome editado em "Editar pessoa", spec 007)
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarPorId(id: Int): Flow<UsuarioEntity?>

    // Perfil gravado pelo name (PerfilUsuario); busca do wizard "Novo projeto" (orientador e alunos)
    @Query("SELECT * FROM usuarios WHERE perfil = :perfil AND ativo = 1")
    fun observarAtivosPorPerfil(perfil: String): Flow<List<UsuarioEntity>>

    // Tela Pessoas (spec 007): filtro, busca e ordem por nome ficam no ViewModel (Collator pt-BR)
    @Query("SELECT * FROM usuarios")
    fun observarTodos(): Flow<List<UsuarioEntity>>

    // ABORT: e-mail repetido viola o índice único em vez de sobrescrever a conta
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(usuario: UsuarioEntity): Long

    @Update
    suspend fun atualizar(usuario: UsuarioEntity)

    // ===== Administração de pessoas (spec 007) =====

    @Query("SELECT COUNT(*) FROM usuarios WHERE perfil = 'ADMINISTRADOR'")
    suspend fun contarAdministradores(): Int

    // Diálogo 10c: projetos não finalizados que a pessoa orienta
    @Query("SELECT titulo FROM projetos WHERE orientador_id = :id AND estagio != 'FINALIZADO' ORDER BY titulo")
    suspend fun buscarTitulosOrientadosAtivos(id: Int): List<String>

    // Aluno só está em um projeto não finalizado por vez (05-dominio.md)
    @Query(
        "SELECT p.nome_grupo FROM projeto_participantes pp " +
            "INNER JOIN projetos p ON p.id = pp.projeto_id " +
            "WHERE pp.aluno_id = :id AND p.estagio != 'FINALIZADO' LIMIT 1"
    )
    suspend fun buscarGrupoParticipacaoAtiva(id: Int): String?

    // Primeiro cadastro num banco sem nenhum Administrador nasce Administrador (spec 007, HU-06).
    // Contagem e insert na mesma transação: dois cadastros simultâneos não criam dois administradores
    @Transaction
    suspend fun inserirCadastro(usuario: UsuarioEntity): UsuarioEntity {
        val perfil = if (contarAdministradores() == 0) PerfilUsuario.ADMINISTRADOR else PerfilUsuario.ALUNO
        val novo = usuario.copy(perfil = perfil.name)
        val id = inserir(novo)
        return novo.copy(id = id.toInt())
    }

    // Edição do Administrador: confere o próprio cadastro e os vínculos com projetos ativos na mesma
    // transação da gravação. E-mail, senha e aceite dos termos vêm sempre do banco, nunca da tela
    @Transaction
    suspend fun salvarEdicao(
        editorId: Int,
        id: Int,
        nome: String,
        perfil: PerfilUsuario,
        ativo: Boolean,
        ra: String?,
        curso: String?
    ): UsuarioEntity {
        val atual = buscarPorId(id) ?: throw ConflitoEdicaoPessoa.NaoEncontrada()
        if (id == editorId && (perfil.name != atual.perfil || !ativo)) throw ConflitoEdicaoPessoa.EdicaoPropria()

        bloqueioEdicaoPessoa(
            atual = atual.toUsuario(),
            perfil = perfil,
            ativo = ativo,
            projetosOrientadosAtivos = buscarTitulosOrientadosAtivos(id),
            grupoParticipacaoAtiva = buscarGrupoParticipacaoAtiva(id)
        )?.let { throw ConflitoEdicaoPessoa.Bloqueada(it) }

        val atualizado = atual.copy(nome = nome, perfil = perfil.name, ativo = ativo, ra = ra, curso = curso)
        atualizar(atualizado)
        return atualizado
    }

    // ===== Meu perfil (spec 008) =====

    // Autoedição: só nome e, se a linha for Aluno, RA e curso. Perfil, e-mail, ativo, senha e aceite dos
    // termos vêm sempre do banco, nunca da tela. RA/curso ausentes mantêm o gravado: na autoedição são
    // obrigatórios (spec 008), o aluno não volta a "Cadastro incompleto". Retorna null se a linha não existe
    @Transaction
    suspend fun salvarDadosProprios(id: Int, nome: String, ra: String?, curso: String?): UsuarioEntity? {
        val atual = buscarPorId(id) ?: return null
        val aluno = atual.perfil == PerfilUsuario.ALUNO.name
        val atualizado = atual.copy(
            nome = nome,
            ra = if (aluno) ra?.takeIf { it.isNotBlank() } ?: atual.ra else atual.ra,
            curso = if (aluno) curso ?: atual.curso else atual.curso
        )
        atualizar(atualizado)
        return atualizado
    }

    // Só grava se o hash ainda for o que foi verificado: uma troca feita no meio-tempo não é sobrescrita.
    // Retorna o número de linhas alteradas (0 = senha mudou ou usuário não existe)
    @Query("UPDATE usuarios SET senha_hash = :hashNovo WHERE id = :id AND senha_hash = :hashAtual")
    suspend fun atualizarSenhaSeConfere(id: Int, hashAtual: String, hashNovo: String): Int
}
