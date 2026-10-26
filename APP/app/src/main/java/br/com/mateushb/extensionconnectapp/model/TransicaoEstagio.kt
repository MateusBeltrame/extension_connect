package br.com.mateushb.extensionconnectapp.model

// Mudanças de estágio possíveis (spec 004): sempre para o estágio vizinho, nunca salta dois
enum class TransicaoEstagio(
    val origem: EstagioProjeto,
    val destino: EstagioProjeto,
    val voltar: Boolean,
    val rotuloMenu: String,
    val mensagemSucesso: String
) {
    AVANCAR_PARA_APLICACAO(
        EstagioProjeto.EM_ANALISE, EstagioProjeto.EM_APLICACAO, false,
        "Avançar para Em aplicação", "Projeto movido para Em aplicação"
    ),
    FINALIZAR(
        EstagioProjeto.EM_APLICACAO, EstagioProjeto.FINALIZADO, false,
        "Avançar para Finalizado", "Projeto finalizado"
    ),
    VOLTAR_PARA_ANALISE(
        EstagioProjeto.EM_APLICACAO, EstagioProjeto.EM_ANALISE, true,
        "Voltar para Em análise", "Projeto voltou para Em análise"
    ),
    VOLTAR_PARA_APLICACAO(
        EstagioProjeto.FINALIZADO, EstagioProjeto.EM_APLICACAO, true,
        "Voltar para Em aplicação", "Projeto voltou para Em aplicação"
    );

    companion object {
        const val MIN_JUSTIFICATIVA = 10

        // Obrigatória só ao voltar; conta sem os espaços das pontas
        fun erroJustificativa(texto: String): String? =
            "A justificativa deve ter ao menos $MIN_JUSTIFICATIVA caracteres".takeIf { texto.trim().length < MIN_JUSTIFICATIVA }
    }
}
