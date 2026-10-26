package br.com.mateushb.extensionconnectapp.model

// Aluno ativo na busca do Passo 3; grupoAtivo = nome do grupo do projeto não finalizado de que já participa
data class CandidatoParticipante(
    val aluno: Usuario,
    val grupoAtivo: String? = null
) {
    val bloqueado: Boolean
        get() = grupoAtivo != null
}
