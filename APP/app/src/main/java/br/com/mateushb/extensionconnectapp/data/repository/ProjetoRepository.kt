package br.com.mateushb.extensionconnectapp.data.repository

import br.com.mateushb.extensionconnectapp.data.local.ConflitoNovoProjeto
import br.com.mateushb.extensionconnectapp.data.local.dao.ProjetoDao
import br.com.mateushb.extensionconnectapp.data.local.dao.UsuarioDao
import br.com.mateushb.extensionconnectapp.data.local.entity.EtapaEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.HistoricoEstagioEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.ProjetoEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.UsuarioEntity
import br.com.mateushb.extensionconnectapp.model.CandidatoParticipante
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.model.ResultadoSalvarProjeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.Collator
import java.time.Instant
import java.util.Locale

class ProjetoRepository(
    private val projetoDao: ProjetoDao,
    private val usuarioDao: UsuarioDao
) {

    private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

    // Aluno vê só os projetos de que participa (inclusive finalizados); Professor e Administrador
    // veem todos (spec 002, HU-01)
    fun projetosVisiveisEmTempoReal(usuario: Usuario): Flow<List<ProjetoResumo>> {
        val origem = if (usuario.perfil == PerfilUsuario.ALUNO) {
            projetoDao.observarProjetosCompletosDoAluno(usuario.id)
        } else {
            projetoDao.observarProjetosCompletos()
        }
        return origem.map { lista -> lista.map { it.toProjetoResumo() } }
    }

    fun projetoEmTempoReal(id: Int): Flow<Projeto?> =
        projetoDao.observarProjetoPorId(id).map { it?.toProjeto() }

    // ===== Wizard "Novo projeto" (spec 003) =====

    val professoresAtivosEmTempoReal: Flow<List<Usuario>> =
        usuarioDao.observarAtivosPorPerfil(PerfilUsuario.PROFESSOR.name).map { ordenarSemSenha(it) }

    // Alunos ativos; quem já está num projeto não finalizado vem com o nome do grupo (bloqueado)
    val candidatosParticipantesEmTempoReal: Flow<List<CandidatoParticipante>> =
        combine(
            usuarioDao.observarAtivosPorPerfil(PerfilUsuario.ALUNO.name),
            projetoDao.observarParticipacoesAtivas()
        ) { alunos, participacoes ->
            val grupoPorAluno = participacoes.associate { it.alunoId to it.nomeGrupo }
            ordenarSemSenha(alunos).map { CandidatoParticipante(aluno = it, grupoAtivo = grupoPorAluno[it.id]) }
        }

    // O que pode ter mudado desde que a tela carregou (orientador ativo, alunos ativos e livres) é
    // conferido dentro da mesma transação da gravação de projeto + etapas + participantes + histórico
    suspend fun salvarNovoProjeto(rascunho: RascunhoProjeto, autorId: Int): ResultadoSalvarProjeto {
        val participantes = rascunho.participantes
        return try {
            val projetoId = projetoDao.inserirProjetoCompleto(
                projeto = ProjetoEntity.fromProjeto(rascunho.toProjeto()),
                etapas = rascunho.toEtapas().map { EtapaEntity.fromEtapa(it) },
                alunoIds = participantes.map { it.id },
                historico = HistoricoEstagioEntity(
                    projetoId = 0,
                    estagio = EstagioProjeto.EM_ANALISE.name,
                    autorId = autorId,
                    data = Instant.now().toString()
                )
            )
            ResultadoSalvarProjeto.Sucesso(projetoId)
        } catch (conflito: ConflitoNovoProjeto) {
            when (conflito) {
                is ConflitoNovoProjeto.OrientadorInativo -> ResultadoSalvarProjeto.OrientadorInativo
                is ConflitoNovoProjeto.AlunoInativo ->
                    ResultadoSalvarProjeto.AlunoInativo(participantes.first { it.id == conflito.alunoId })
                is ConflitoNovoProjeto.AlunoOcupado ->
                    ResultadoSalvarProjeto.AlunoIndisponivel(
                        aluno = participantes.first { it.id == conflito.alunoId },
                        nomeGrupo = conflito.nomeGrupo
                    )
            }
        }
    }

    // Hash de senha não sai do Repository para telas de outros usuários
    private fun ordenarSemSenha(lista: List<UsuarioEntity>): List<Usuario> =
        lista.map { it.toUsuario().copy(senhaHash = "") }.sortedWith(compareBy(collator) { it.nome })
}
