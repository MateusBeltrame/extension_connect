package br.com.mateushb.extensionconnectapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Usuario

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "senha_hash")
    val senhaHash: String,

    // Enums gravados pelo name
    @ColumnInfo(name = "perfil")
    val perfil: String,

    @ColumnInfo(name = "ativo")
    val ativo: Boolean,

    @ColumnInfo(name = "ra")
    val ra: String? = null,

    @ColumnInfo(name = "curso")
    val curso: String? = null,

    @ColumnInfo(name = "aceitou_termos_em")
    val aceitouTermosEm: String? = null
) {
    fun toUsuario(): Usuario = Usuario(
        id = id,
        nome = nome,
        email = email,
        senhaHash = senhaHash,
        perfil = PerfilUsuario.entries.firstOrNull { it.name == perfil } ?: PerfilUsuario.ALUNO,
        ativo = ativo,
        ra = ra,
        curso = Curso.entries.firstOrNull { it.name == curso },
        aceitouTermosEm = aceitouTermosEm
    )

    companion object {
        fun fromUsuario(usuario: Usuario): UsuarioEntity = UsuarioEntity(
            id = usuario.id,
            nome = usuario.nome,
            email = usuario.email,
            senhaHash = usuario.senhaHash,
            perfil = usuario.perfil.name,
            ativo = usuario.ativo,
            ra = usuario.ra,
            curso = usuario.curso?.name,
            aceitouTermosEm = usuario.aceitouTermosEm
        )
    }
}
