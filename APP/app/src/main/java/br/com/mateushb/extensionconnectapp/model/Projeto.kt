package br.com.mateushb.extensionconnectapp.model

// Datas como LocalDate ISO ("2026-10-25"); sem @SerializedName (projeto sem Gson, ver 07-dados.md)
data class Projeto(
    val id: Int = 0,
    val titulo: String = "",
    val nomeGrupo: String = "",
    val descricao: String = "",
    val tema: TemaProjeto = TemaProjeto.EDUCACAO,
    val orientadorId: Int = 0,
    val dataInicio: String = "",
    val dataFimPrevisto: String = "",
    val estagio: EstagioProjeto = EstagioProjeto.EM_ANALISE,
    val aplicacaoInicio: String? = null,
    val aplicacaoFim: String? = null,
    val aplicacaoDescricao: String? = null,
    val aguardandoCienciaOrientador: Boolean = false
)
