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
import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.RascunhoAplicacao
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.model.ResultadoMudancaEstagio
import br.com.mateushb.extensionconnectapp.model.ResultadoSalvarProjeto
import br.com.mateushb.extensionconnectapp.model.TransicaoEstagio
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.model.lerInstanteIso
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
        // O diário entra só aqui (regra "parado", spec 005): uma consulta leve, sem os textos
        return combine(origem, projetoDao.observarDatasDiario()) { lista, datasDiario ->
            val ultimaPorProjeto = datasDiario
                .groupBy { it.projetoId }
                .mapValues { (_, datas) -> datas.maxByOrNull { lerInstanteIso(it.data) ?: Instant.MIN }?.data }
            lista.map { it.toProjetoResumo(ultimaPorProjeto[it.projeto.id]) }
        }
    }

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

    // ===== Detalhes e ciclo de estágio (spec 004) =====

    fun projetoDetalhesEmTempoReal(id: Int): Flow<ProjetoDetalhes?> =
        projetoDao.observarProjetoCompletoPorId(id).map { it?.toProjetoDetalhes() }

    // Quem não é o orientador liga "Aguardando ciência"; o próprio orientador desliga (HU-09).
    // Aplicação só ao avançar para Em aplicação; justificativa só ao voltar
    suspend fun mudarEstagio(
        projeto: Projeto,
        transicao: TransicaoEstagio,
        autor: Usuario,
        aplicacao: RascunhoAplicacao?,
        justificativa: String?
    ): ResultadoMudancaEstagio {
        // A validação do sheet é repetida aqui: Em aplicação sem período, ou volta sem justificativa,
        // não chegam ao banco por nenhum caminho (revisão da spec 004)
        if (transicao == TransicaoEstagio.AVANCAR_PARA_APLICACAO) {
            require(aplicacao != null && aplicacao.erros(projeto).valido) { "Dados de aplicação inválidos" }
        }
        if (transicao.voltar) {
            require(justificativa != null && TransicaoEstagio.erroJustificativa(justificativa) == null) {
                "Justificativa inválida"
            }
        }
        val dadosAplicacao = aplicacao.takeIf { transicao == TransicaoEstagio.AVANCAR_PARA_APLICACAO }
        val gravou = projetoDao.mudarEstagio(
            projetoId = projeto.id,
            origem = transicao.origem.name,
            destino = transicao.destino.name,
            aguardandoCiencia = autor.id != projeto.orientadorId,
            aplicacaoInicio = dadosAplicacao?.inicio?.toString(),
            aplicacaoFim = dadosAplicacao?.fim?.toString(),
            aplicacaoDescricao = dadosAplicacao?.descricao?.trim(),
            historico = HistoricoEstagioEntity(
                projetoId = projeto.id,
                estagio = transicao.destino.name,
                autorId = autor.id,
                data = Instant.now().toString(),
                justificativa = justificativa?.trim().takeIf { transicao.voltar }
            )
        )
        return if (gravou) ResultadoMudancaEstagio.Sucesso else ResultadoMudancaEstagio.EstagioMudou
    }

    suspend fun confirmarCiencia(projetoId: Int) {
        projetoDao.confirmarCiencia(projetoId)
    }

    suspend fun alternarConclusaoEtapa(etapaId: Int) {
        projetoDao.alternarConclusaoEtapa(etapaId)
    }

    // Hash de senha não sai do Repository para telas de outros usuários
    private fun ordenarSemSenha(lista: List<UsuarioEntity>): List<Usuario> =
        lista.map { it.toUsuario().copy(senhaHash = "") }.sortedWith(compareBy(collator) { it.nome })
}
