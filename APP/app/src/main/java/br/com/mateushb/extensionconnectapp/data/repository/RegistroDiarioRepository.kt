package br.com.mateushb.extensionconnectapp.data.repository

import br.com.mateushb.extensionconnectapp.data.local.dao.RegistroDiarioDao
import br.com.mateushb.extensionconnectapp.data.local.entity.RegistroDiarioEntity
import br.com.mateushb.extensionconnectapp.model.RegistroDiario
import br.com.mateushb.extensionconnectapp.model.RegistroDiarioDetalhes
import br.com.mateushb.extensionconnectapp.model.ResultadoPublicarRegistro
import br.com.mateushb.extensionconnectapp.model.TipoRegistro
import br.com.mateushb.extensionconnectapp.model.lerInstanteIso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

// Diário do projeto (spec 005)
class RegistroDiarioRepository(private val registroDiarioDao: RegistroDiarioDao) {

    // Do mais recente para o mais antigo, comparando como Instant: o texto ISO muda de tamanho com a
    // fração de segundo e não ordena certo. Empate (ou data inválida) cai no id, a ordem de gravação
    fun registrosEmTempoReal(projetoId: Int): Flow<List<RegistroDiarioDetalhes>> =
        registroDiarioDao.observarRegistrosPorProjeto(projetoId).map { lista ->
            lista.map { it.toRegistroDiarioDetalhes() }.sortedWith(
                compareByDescending<RegistroDiarioDetalhes> { lerInstanteIso(it.registro.data) ?: Instant.MIN }
                    .thenByDescending { it.registro.id }
            )
        }

    // A validação do sheet é repetida aqui: texto curto não chega ao banco por nenhum caminho.
    // Projeto, papel e etapa são conferidos pelo DAO na mesma transação da gravação
    suspend fun publicar(
        projetoId: Int,
        autorId: Int,
        tipo: TipoRegistro,
        texto: String,
        etapaId: Int?
    ): ResultadoPublicarRegistro {
        require(RegistroDiario.erroTexto(texto) == null) { "Texto do registro inválido" }
        val registro = RegistroDiario(
            projetoId = projetoId,
            autorId = autorId,
            data = Instant.now().toString(),
            tipo = tipo,
            etapaId = etapaId,
            texto = texto.trim()
        )
        val gravou = registroDiarioDao.publicar(RegistroDiarioEntity.fromRegistroDiario(registro))
        return if (gravou) ResultadoPublicarRegistro.Sucesso else ResultadoPublicarRegistro.SemPermissao
    }

    // false = nada apagado (registro de outra pessoa, já excluído ou projeto finalizado)
    suspend fun excluir(registroId: Int, autorId: Int): Boolean =
        registroDiarioDao.excluirDoAutor(registroId, autorId) > 0
}
