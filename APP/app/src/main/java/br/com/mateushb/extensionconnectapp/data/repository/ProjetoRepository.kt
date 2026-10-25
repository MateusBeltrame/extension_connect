package br.com.mateushb.extensionconnectapp.data.repository

import br.com.mateushb.extensionconnectapp.data.local.dao.ProjetoDao
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjetoRepository(private val projetoDao: ProjetoDao) {

    // Aluno vê só os projetos de que participa (inclusive finalizados); Professor e Administrador
    // veem todos (spec 002, HU-01)
    fun projetosVisiveisEmTempoReal(usuario: Usuario): Flow<List<ProjetoResumo>> {
        val origem = if (usuario.perfil == PerfilUsuario.ALUNO) {
            projetoDao.observarProjetosCompletosDoAluno(usuario.id)
        } else {
            projetoDao.observarProjetosCompletos()
        }
        return origem.map { lista -> lista.map { it.toProjetoResumo() } }
    }
}
