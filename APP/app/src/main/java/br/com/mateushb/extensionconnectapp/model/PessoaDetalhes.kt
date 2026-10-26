package br.com.mateushb.extensionconnectapp.model

// Pessoa aberta em "Editar pessoa" com os vínculos que podem bloquear a edição (spec 007). Lido uma vez
// ao abrir a tela; ao salvar, o UsuarioDao confere de novo na mesma transação
data class PessoaDetalhes(
    val usuario: Usuario,
    val projetosOrientadosAtivos: List<String> = emptyList(),
    val grupoParticipacaoAtiva: String? = null
) {
    // Bloqueio que a combinação perfil/ativo escolhida na tela causaria, ou null se pode gravar
    fun bloqueioPara(perfil: PerfilUsuario, ativo: Boolean): BloqueioEdicaoPessoa? =
        bloqueioEdicaoPessoa(usuario, perfil, ativo, projetosOrientadosAtivos, grupoParticipacaoAtiva)
}

// Regra única da HU-04, usada pela tela (PessoaDetalhes) e pela transação do UsuarioDao
fun bloqueioEdicaoPessoa(
    atual: Usuario,
    perfil: PerfilUsuario,
    ativo: Boolean,
    projetosOrientadosAtivos: List<String>,
    grupoParticipacaoAtiva: String?
): BloqueioEdicaoPessoa? {
    val deixaDeOrientar = perfil != PerfilUsuario.PROFESSOR || (atual.ativo && !ativo)
    if (projetosOrientadosAtivos.isNotEmpty() && deixaDeOrientar) {
        return BloqueioEdicaoPessoa.OrientadorComProjetosAtivos(projetosOrientadosAtivos)
    }
    if (grupoParticipacaoAtiva != null && atual.perfil == PerfilUsuario.ALUNO && perfil != PerfilUsuario.ALUNO) {
        return BloqueioEdicaoPessoa.ParticipanteDeProjetoAtivo(grupoParticipacaoAtiva)
    }
    return null
}
