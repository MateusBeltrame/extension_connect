package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import br.com.mateushb.extensionconnectapp.model.RegistroDiarioDetalhes

// Registro do diário + quem o publicou + a etapa relacionada, para os cards da aba Diário (spec 005)
data class RegistroDiarioComAutor(
    @Embedded
    val registro: RegistroDiarioEntity,

    @Relation(
        parentColumn = "autor_id",
        entityColumn = "id"
    )
    val autor: UsuarioEntity?,

    @Relation(
        parentColumn = "etapa_id",
        entityColumn = "id"
    )
    val etapa: EtapaEntity?
) {
    // O hash de senha não sai da camada de dados (mesma regra do ProjetoComRelacoes)
    fun toRegistroDiarioDetalhes(): RegistroDiarioDetalhes = RegistroDiarioDetalhes(
        registro = registro.toRegistroDiario(),
        autor = autor?.toUsuario()?.copy(senhaHash = ""),
        etapa = etapa?.toEtapa()
    )
}
