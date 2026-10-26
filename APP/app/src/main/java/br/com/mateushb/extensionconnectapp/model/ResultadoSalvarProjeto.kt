package br.com.mateushb.extensionconnectapp.model

// Revalidação feita no banco antes de gravar o projeto (spec 003, HU-05)
sealed interface ResultadoSalvarProjeto {
    data class Sucesso(val projetoId: Int) : ResultadoSalvarProjeto
    data class AlunoIndisponivel(val aluno: Usuario, val nomeGrupo: String) : ResultadoSalvarProjeto
    data class AlunoInativo(val aluno: Usuario) : ResultadoSalvarProjeto
    data object OrientadorInativo : ResultadoSalvarProjeto
}
