package br.com.mateushb.extensionconnectapp.model

import java.time.LocalDate

// Etapa ainda não gravada; chave estável (não é o id do banco) para arrastar e para os erros por etapa
data class RascunhoEtapa(
    val chave: Long,
    val nome: String = "",
    val dataPrevista: LocalDate? = null
)

// Uma mensagem por campo do Passo 1; null = campo válido
data class ErrosPasso1(
    val nomeGrupo: String? = null,
    val titulo: String? = null,
    val descricao: String? = null,
    val tema: String? = null,
    val orientador: String? = null,
    val dataInicio: String? = null,
    val dataFimPrevisto: String? = null
) {
    val valido: Boolean
        get() = listOf(nomeGrupo, titulo, descricao, tema, orientador, dataInicio, dataFimPrevisto).all { it == null }
}

// Erros do Passo 2: lista vazia + mensagens de nome/data por chave de etapa. Data fora do período
// bloqueia, mas a mensagem fica no próprio campo (foraDoPeriodo), não aqui
data class ErrosPasso2(
    val semEtapas: String? = null,
    val nomes: Map<Long, String> = emptyMap(),
    val datas: Map<Long, String> = emptyMap(),
    val algumaForaDoPeriodo: Boolean = false
) {
    val valido: Boolean
        get() = semEtapas == null && nomes.isEmpty() && datas.isEmpty() && !algumaForaDoPeriodo
}

// Dados dos 3 passos do wizard "Novo projeto" (spec 003) antes de gravar
data class RascunhoProjeto(
    val nomeGrupo: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val tema: TemaProjeto? = null,
    val orientador: Usuario? = null,
    val dataInicio: LocalDate? = null,
    val dataFimPrevisto: LocalDate? = null,
    val etapas: List<RascunhoEtapa> = emptyList(),
    val participantes: List<Usuario> = emptyList()
) {
    val limiteAtingido: Boolean
        get() = participantes.size >= LIMITE_PARTICIPANTES

    // Sem período completo não há como estar "fora" dele
    fun foraDoPeriodo(etapa: RascunhoEtapa): Boolean {
        val data = etapa.dataPrevista ?: return false
        val inicio = dataInicio ?: return false
        val fim = dataFimPrevisto ?: return false
        return data.isBefore(inicio) || data.isAfter(fim)
    }

    fun sugestaoUsada(nome: String): Boolean {
        val termo = ProjetoResumo.normalizar(nome)
        return etapas.any { ProjetoResumo.normalizar(it.nome) == termo }
    }

    // Algo além do orientador automático: fechar pede confirmação (HU-06)
    fun preenchido(orientadorInicial: Usuario?): Boolean =
        nomeGrupo.isNotBlank() || titulo.isNotBlank() || descricao.isNotBlank() || tema != null ||
            orientador?.id != orientadorInicial?.id || dataInicio != null || dataFimPrevisto != null ||
            etapas.isNotEmpty() || participantes.isNotEmpty()

    fun errosPasso1(): ErrosPasso1 = ErrosPasso1(
        nomeGrupo = "Nome do grupo é obrigatório".takeIf { nomeGrupo.isBlank() },
        titulo = "Título é obrigatório".takeIf { titulo.isBlank() },
        descricao = "Descrição é obrigatória".takeIf { descricao.isBlank() },
        tema = "Selecione o tema".takeIf { tema == null },
        orientador = "Selecione o orientador".takeIf { orientador == null },
        dataInicio = "Informe a data de início".takeIf { dataInicio == null },
        dataFimPrevisto = when {
            dataFimPrevisto == null -> "Informe o fim previsto"
            dataInicio != null && !dataFimPrevisto.isAfter(dataInicio) -> "Fim previsto deve ser depois da data de início"
            else -> null
        }
    )

    fun errosPasso2(): ErrosPasso2 = ErrosPasso2(
        semEtapas = "Adicione ao menos uma etapa".takeIf { etapas.isEmpty() },
        nomes = etapas.filter { it.nome.isBlank() }.associate { it.chave to "Nome da etapa é obrigatório" },
        datas = etapas.filter { it.dataPrevista == null }.associate { it.chave to "Informe a data prevista" },
        algumaForaDoPeriodo = etapas.any { foraDoPeriodo(it) }
    )

    fun errosPasso3(): String? = "Adicione ao menos um participante".takeIf { participantes.isEmpty() }

    // Só chamado depois das validações: campos obrigatórios já preenchidos
    fun toProjeto(): Projeto = Projeto(
        titulo = titulo.trim(),
        nomeGrupo = nomeGrupo.trim(),
        descricao = descricao.trim(),
        tema = tema ?: TemaProjeto.EDUCACAO,
        orientadorId = orientador?.id ?: 0,
        dataInicio = dataInicio?.toString().orEmpty(),
        dataFimPrevisto = dataFimPrevisto?.toString().orEmpty(),
        estagio = EstagioProjeto.EM_ANALISE
    )

    // Ordem do cronograma = posição na tela
    fun toEtapas(): List<Etapa> = etapas.mapIndexed { indice, etapa ->
        Etapa(
            nome = etapa.nome.trim(),
            dataPrevista = etapa.dataPrevista?.toString().orEmpty(),
            ordem = indice
        )
    }

    companion object {
        const val MAX_NOME_GRUPO = 50
        const val MAX_TITULO = 100
        const val MAX_DESCRICAO = 1000
        const val LIMITE_PARTICIPANTES = 10
        const val MIN_LETRAS_BUSCA = 3

        val SUGESTOES_ETAPA = listOf(
            "Planejamento", "Preparação de materiais", "Aplicação", "Relatório final", "Apresentação"
        )
    }
}
