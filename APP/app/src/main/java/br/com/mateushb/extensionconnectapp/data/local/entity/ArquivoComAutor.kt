package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import br.com.mateushb.extensionconnectapp.model.ArquivoDetalhes

// Documento + quem enviou, para o card Documentos (spec 006)
data class ArquivoComAutor(
    @Embedded
    val arquivo: ArquivoEntity,

    @Relation(
        parentColumn = "autor_id",
        entityColumn = "id"
    )
    val autor: UsuarioEntity?
) {
    // O hash de senha não sai da camada de dados (mesma regra do ProjetoComRelacoes)
    fun toArquivoDetalhes(): ArquivoDetalhes = ArquivoDetalhes(
        arquivo = arquivo.toArquivo(),
        autor = autor?.toUsuario()?.copy(senhaHash = "")
    )
}
