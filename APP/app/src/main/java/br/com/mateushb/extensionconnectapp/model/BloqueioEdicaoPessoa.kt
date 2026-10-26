package br.com.mateushb.extensionconnectapp.model

// Mudança de perfil/ativo que deixaria projetos não finalizados em estado inválido (spec 007, HU-04)
sealed interface BloqueioEdicaoPessoa {
    // Diálogo 10c: professor que orienta projetos ativos não pode sair de Professor nem ser desativado
    data class OrientadorComProjetosAtivos(val titulos: List<String>) : BloqueioEdicaoPessoa

    // Aluno participante de projeto ativo não vira Professor/Administrador
    data class ParticipanteDeProjetoAtivo(val nomeGrupo: String) : BloqueioEdicaoPessoa
}
