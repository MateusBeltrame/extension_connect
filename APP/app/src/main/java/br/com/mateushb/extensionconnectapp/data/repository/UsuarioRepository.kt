package br.com.mateushb.extensionconnectapp.data.repository

import br.com.mateushb.extensionconnectapp.data.local.ConflitoEdicaoPessoa
import br.com.mateushb.extensionconnectapp.data.local.HashSenha
import br.com.mateushb.extensionconnectapp.data.local.dao.UsuarioDao
import br.com.mateushb.extensionconnectapp.data.local.entity.UsuarioEntity
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.PessoaDetalhes
import br.com.mateushb.extensionconnectapp.model.ResultadoLogin
import br.com.mateushb.extensionconnectapp.model.ResultadoSalvarPessoa
import br.com.mateushb.extensionconnectapp.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // Hash de uma senha qualquer: e-mail inexistente também paga o custo do PBKDF2, para o tempo de
    // resposta não revelar quais e-mails estão cadastrados
    private val hashFicticio: String by lazy { HashSenha.gerar("senha-ficticia") }

    suspend fun autenticar(email: String, senha: String): ResultadoLogin {
        val entity = usuarioDao.buscarPorEmail(Usuario.normalizarEmail(email))
        // Mesma resposta (e mesmo custo) para e-mail inexistente e senha errada
        val senhaConfere = verificarSenha(senha, entity?.senhaHash ?: hashFicticio)
        if (entity == null || !senhaConfere) {
            return ResultadoLogin.CredenciaisInvalidas
        }
        if (!entity.ativo) return ResultadoLogin.ContaDesativada
        return ResultadoLogin.Sucesso(entity.toUsuario())
    }

    suspend fun emailJaCadastrado(email: String): Boolean {
        return usuarioDao.buscarPorEmail(Usuario.normalizarEmail(email)) != null
    }

    // Cadastro pelo app nasce Aluno (RA e curso ficam para "Completar cadastro"), exceto o primeiro
    // num banco sem Administrador, que nasce Administrador (spec 007, HU-06)
    suspend fun cadastrar(nome: String, email: String, senha: String): Usuario {
        val usuario = Usuario(
            nome = nome.trim(),
            email = Usuario.normalizarEmail(email),
            senhaHash = withContext(Dispatchers.Default) { HashSenha.gerar(senha) },
            perfil = PerfilUsuario.ALUNO,
            ativo = true,
            aceitouTermosEm = Instant.now().toString()
        )
        return usuarioDao.inserirCadastro(UsuarioEntity.fromUsuario(usuario)).toUsuario()
    }

    suspend fun completarCadastro(usuarioId: Int, ra: String, curso: Curso): Usuario {
        val entity = usuarioDao.buscarPorId(usuarioId)
            ?: throw IllegalStateException("Usuário não encontrado no banco local.")
        val atualizado = entity.toUsuario().copy(ra = ra.trim(), curso = curso)
        usuarioDao.atualizar(UsuarioEntity.fromUsuario(atualizado))
        return atualizado
    }

    suspend fun obterPorId(id: Int): Usuario? {
        return usuarioDao.buscarPorId(id)?.toUsuario()
    }

    fun observarPorId(id: Int): Flow<Usuario?> = usuarioDao.observarPorId(id).map { it?.toUsuario() }

    // ===== Administração de pessoas (spec 007) =====

    val usuariosEmTempoReal: Flow<List<Usuario>> = usuarioDao.observarTodos().map { lista ->
        lista.map { it.toUsuario() }
    }

    suspend fun obterPessoaDetalhes(id: Int): PessoaDetalhes? {
        val usuario = usuarioDao.buscarPorId(id)?.toUsuario() ?: return null
        return PessoaDetalhes(
            usuario = usuario,
            projetosOrientadosAtivos = usuarioDao.buscarTitulosOrientadosAtivos(id),
            grupoParticipacaoAtiva = usuarioDao.buscarGrupoParticipacaoAtiva(id)
        )
    }

    // RA e curso só existem para Aluno; RA em branco vira null (fica "Cadastro incompleto")
    suspend fun salvarPessoa(editor: Usuario, pessoa: Usuario): ResultadoSalvarPessoa {
        val aluno = pessoa.perfil == PerfilUsuario.ALUNO
        return try {
            val salvo = usuarioDao.salvarEdicao(
                editorId = editor.id,
                id = pessoa.id,
                nome = pessoa.nome.trim(),
                perfil = pessoa.perfil,
                ativo = pessoa.ativo,
                ra = pessoa.ra?.trim()?.takeIf { aluno && it.isNotEmpty() },
                curso = pessoa.curso?.name?.takeIf { aluno }
            )
            ResultadoSalvarPessoa.Sucesso(salvo.toUsuario())
        } catch (_: ConflitoEdicaoPessoa.NaoEncontrada) {
            ResultadoSalvarPessoa.NaoEncontrada
        } catch (_: ConflitoEdicaoPessoa.EdicaoPropria) {
            ResultadoSalvarPessoa.EdicaoPropriaProibida
        } catch (e: ConflitoEdicaoPessoa.Bloqueada) {
            ResultadoSalvarPessoa.Bloqueado(e.bloqueio)
        }
    }

    // PBKDF2 é custoso de propósito: roda fora da thread principal
    private suspend fun verificarSenha(senha: String, senhaHash: String): Boolean =
        withContext(Dispatchers.Default) { HashSenha.verificar(senha, senhaHash) }
}
