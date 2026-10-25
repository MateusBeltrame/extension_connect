package br.com.mateushb.extensionconnectapp.data.repository

import br.com.mateushb.extensionconnectapp.data.local.HashSenha
import br.com.mateushb.extensionconnectapp.data.local.dao.UsuarioDao
import br.com.mateushb.extensionconnectapp.data.local.entity.UsuarioEntity
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.ResultadoLogin
import br.com.mateushb.extensionconnectapp.model.Usuario
import kotlinx.coroutines.Dispatchers
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

    // Cadastro pelo app é sempre como Aluno; RA e curso ficam para "Completar cadastro"
    suspend fun cadastrarAluno(nome: String, email: String, senha: String): Usuario {
        val usuario = Usuario(
            nome = nome.trim(),
            email = Usuario.normalizarEmail(email),
            senhaHash = withContext(Dispatchers.Default) { HashSenha.gerar(senha) },
            perfil = PerfilUsuario.ALUNO,
            ativo = true,
            aceitouTermosEm = Instant.now().toString()
        )
        val id = usuarioDao.inserir(UsuarioEntity.fromUsuario(usuario))
        return usuario.copy(id = id.toInt())
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

    // PBKDF2 é custoso de propósito: roda fora da thread principal
    private suspend fun verificarSenha(senha: String, senhaHash: String): Boolean =
        withContext(Dispatchers.Default) { HashSenha.verificar(senha, senhaHash) }
}
