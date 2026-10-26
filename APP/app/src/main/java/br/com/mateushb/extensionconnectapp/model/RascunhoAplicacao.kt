package br.com.mateushb.extensionconnectapp.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Uma mensagem por campo do sheet "Mover para Em aplicação"; null = campo válido
data class ErrosAplicacao(
    val inicio: String? = null,
    val fim: String? = null,
    val descricao: String? = null
) {
    val valido: Boolean
        get() = inicio == null && fim == null && descricao == null
}

// Dados do sheet 7a (spec 004, HU-08) antes de gravar
data class RascunhoAplicacao(
    val inicio: LocalDate? = null,
    val fim: LocalDate? = null,
    val descricao: String = ""
) {
    // A aplicação precisa caber no período do projeto (bloqueia, como as etapas na spec 003)
    fun erros(projeto: Projeto): ErrosAplicacao {
        val inicioProjeto = lerDataIso(projeto.dataInicio)
        val fimProjeto = lerDataIso(projeto.dataFimPrevisto)
        val foraDoPeriodo = { data: LocalDate ->
            inicioProjeto != null && fimProjeto != null && (data.isBefore(inicioProjeto) || data.isAfter(fimProjeto))
        }
        val mensagemPeriodo = if (inicioProjeto != null && fimProjeto != null) {
            "Aplicação deve estar dentro do período do projeto " +
                "(${inicioProjeto.format(FORMATO_DATA)} a ${fimProjeto.format(FORMATO_DATA)})"
        } else {
            null
        }
        return ErrosAplicacao(
            inicio = when {
                inicio == null -> "Informe o início da aplicação"
                foraDoPeriodo(inicio) -> mensagemPeriodo
                else -> null
            },
            fim = when {
                fim == null -> "Informe o fim da aplicação"
                inicio != null && !fim.isAfter(inicio) -> "Fim deve ser depois do início"
                foraDoPeriodo(fim) -> mensagemPeriodo
                else -> null
            },
            descricao = "Descrição da aplicação é obrigatória".takeIf { descricao.isBlank() }
        )
    }

    companion object {
        const val MAX_DESCRICAO = 120

        private val FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Pré-preenche com a aplicação anterior, quando o projeto já voltou de estágio
        fun de(projeto: Projeto): RascunhoAplicacao = RascunhoAplicacao(
            inicio = lerDataIso(projeto.aplicacaoInicio),
            fim = lerDataIso(projeto.aplicacaoFim),
            descricao = projeto.aplicacaoDescricao.orEmpty().take(MAX_DESCRICAO)
        )
    }
}
