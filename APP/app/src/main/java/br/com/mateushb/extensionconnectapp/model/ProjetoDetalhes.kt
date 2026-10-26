package br.com.mateushb.extensionconnectapp.model

// Uma linha do histórico com o nome de quem mudou e o estágio de antes (null = criação do projeto)
data class EventoHistorico(
    val historico: HistoricoEstagio,
    val autor: Usuario? = null,
    val estagioAnterior: EstagioProjeto? = null
) {
    val criacao: Boolean
        get() = estagioAnterior == null
}

// Agregado da tela Detalhes do projeto (spec 004). Compõe o ProjetoResumo da lista para herdar
// atrasado/progresso; o histórico vem do mais recente para o mais antigo
data class ProjetoDetalhes(
    val resumo: ProjetoResumo,
    val historico: List<EventoHistorico> = emptyList()
) {
    val projeto: Projeto
        get() = resumo.projeto

    val somenteLeitura: Boolean
        get() = projeto.estagio == EstagioProjeto.FINALIZADO

    val etapasPendentes: List<Etapa>
        get() = resumo.etapas.filterNot { it.concluida }

    fun ehOrientador(usuario: Usuario): Boolean = usuario.id == projeto.orientadorId

    fun ehParticipante(usuario: Usuario): Boolean = resumo.participantes.any { it.id == usuario.id }

    // Gestor = orientador ou Administrador: avança, volta e confirma ciência
    fun ehGestor(usuario: Usuario): Boolean = ehOrientador(usuario) || usuario.perfil == PerfilUsuario.ADMINISTRADOR

    // Participante só avança; voltar (inclusive de Finalizado) é do gestor; professor que não orienta não muda
    fun transicoesPara(usuario: Usuario): List<TransicaoEstagio> {
        val gestor = ehGestor(usuario)
        val participante = ehParticipante(usuario)
        return TransicaoEstagio.entries.filter { transicao ->
            transicao.origem == projeto.estagio && (gestor || (participante && !transicao.voltar))
        }
    }

    fun podeConfirmarCiencia(usuario: Usuario): Boolean = projeto.aguardandoCienciaOrientador && ehGestor(usuario)

    fun podeMarcarEtapa(usuario: Usuario): Boolean = !somenteLeitura && (ehGestor(usuario) || ehParticipante(usuario))

    // ===== Diário (spec 005) =====

    // O tipo vem do papel: participante registra Atividade, orientador registra Orientação.
    // Administrador e professor que não orienta só leem; Finalizado é somente leitura (null = não publica)
    fun tipoRegistroPara(usuario: Usuario): TipoRegistro? = when {
        somenteLeitura -> null
        ehOrientador(usuario) -> TipoRegistro.ORIENTACAO
        ehParticipante(usuario) -> TipoRegistro.ATIVIDADE
        else -> null
    }

    fun podeExcluirRegistro(usuario: Usuario, registro: RegistroDiario): Boolean =
        !somenteLeitura && registro.autorId == usuario.id && registro.projetoId == projeto.id

    // ===== Arquivos e fotos (spec 006) =====

    // Participante e orientador enviam; Administrador e professor que não orienta só veem
    fun podeEnviarArquivos(usuario: Usuario): Boolean =
        !somenteLeitura && (ehOrientador(usuario) || ehParticipante(usuario))

    // Excluir e mudar a autorização do Mural: só o autor, com projeto não finalizado
    fun podeGerenciarArquivo(usuario: Usuario, autorId: Int): Boolean = !somenteLeitura && autorId == usuario.id
}
