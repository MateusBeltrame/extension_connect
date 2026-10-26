package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import br.com.mateushb.extensionconnectapp.model.FotoDetalhes

// Foto + quem enviou, para o card Fotos e o sheet da foto (spec 006)
data class FotoComAutor(
    @Embedded
    val foto: FotoEntity,

    @Relation(
        parentColumn = "autor_id",
        entityColumn = "id"
    )
    val autor: UsuarioEntity?
) {
    // O hash de senha não sai da camada de dados (mesma regra do ProjetoComRelacoes)
    fun toFotoDetalhes(): FotoDetalhes = FotoDetalhes(
        foto = foto.toFoto(),
        autor = autor?.toUsuario()?.copy(senhaHash = "")
    )
}
