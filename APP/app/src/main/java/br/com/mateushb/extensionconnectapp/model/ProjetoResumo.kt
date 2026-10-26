package br.com.mateushb.extensionconnectapp.model

import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

// Agregado exibido nos cards da tela Projetos (lista e quadro). As regras recebem "hoje" por
// parâmetro para não depender do relógio e darem o mesmo resultado em toda a tela
data class ProjetoResumo(
    val projeto: Projeto,
    val orientador: Usuario,
    val participantes: List<Usuario> = emptyList(),
    val etapas: List<Etapa> = emptyList(),
    // Instant ISO da última linha do histórico de estágio
    val dataUltimaMudancaEstagio: String? = null,
    // Instant ISO do registro de diário mais recente (spec 005); null = diário vazio
    val dataUltimoRegistroDiario: String? = null
) {
    val etapasConcluidas: Int
        get() = etapas.count { it.concluida }

    val semCronograma: Boolean
        get() = etapas.isEmpty()

    // Arredonda para baixo: 2/3 = 66%
    val percentualConcluido: Int
        get() = if (etapas.isEmpty()) 0 else etapasConcluidas * 100 / etapas.size

    fun diasNoEstagio(hoje: LocalDate): Int? {
        val desde = dataLocalDoInstante(dataUltimaMudancaEstagio) ?: return null
        return ChronoUnit.DAYS.between(desde, hoje).toInt().coerceAtLeast(0)
    }

    fun atrasado(hoje: LocalDate): Boolean {
        val fim = lerDataIso(projeto.aplicacaoFim) ?: return false
        return projeto.estagio == EstagioProjeto.EM_APLICACAO && hoje.isAfter(fim)
    }

    // Atualização = mudança de estágio ou registro no diário, o que for mais recente (spec 005, HU-06).
    // "Há N dias neste estágio" continua contando só a partir da mudança de estágio
    fun parado(hoje: LocalDate): Boolean {
        if (projeto.estagio == EstagioProjeto.FINALIZADO) return false
        val ultimaAtualizacao = listOfNotNull(
            dataLocalDoInstante(dataUltimaMudancaEstagio),
            dataLocalDoInstante(dataUltimoRegistroDiario)
        ).maxOrNull() ?: return false
        return ChronoUnit.DAYS.between(ultimaAtualizacao, hoje) > DIAS_PARA_PARADO
    }

    // Período de aplicação cruza a semana atual (segunda a domingo)
    fun emAplicacaoNaSemana(hoje: LocalDate): Boolean {
        if (projeto.estagio != EstagioProjeto.EM_APLICACAO) return false
        val inicio = lerDataIso(projeto.aplicacaoInicio) ?: return false
        val fim = lerDataIso(projeto.aplicacaoFim) ?: return false
        val segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val domingo = segunda.plusDays(6)
        return !inicio.isAfter(domingo) && !fim.isBefore(segunda)
    }

    // Recebe o termo já passado por normalizar(), para não repetir o trabalho a cada projeto
    fun correspondeABusca(termo: String): Boolean {
        if (termo.isEmpty()) return true
        return normalizar(projeto.titulo).contains(termo) ||
            normalizar(projeto.nomeGrupo).contains(termo) ||
            participantes.any { normalizar(it.nome).contains(termo) }
    }

    companion object {
        const val DIAS_PARA_PARADO = 30

        private val ACENTOS = "\\p{M}+".toRegex()

        // Sem acentos, minúsculo e sem espaços nas pontas: "Raízes" ≈ "raizes"
        fun normalizar(texto: String): String =
            Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD).replace(ACENTOS, "")
    }
}
