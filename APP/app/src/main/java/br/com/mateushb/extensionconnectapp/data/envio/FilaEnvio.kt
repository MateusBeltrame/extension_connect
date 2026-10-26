package br.com.mateushb.extensionconnectapp.data.envio

import android.content.Context
import br.com.mateushb.extensionconnectapp.data.local.AppDatabase
import br.com.mateushb.extensionconnectapp.data.local.dao.ArquivoDao
import br.com.mateushb.extensionconnectapp.data.local.dao.FotoDao
import br.com.mateushb.extensionconnectapp.model.EnvioEmAndamento
import br.com.mateushb.extensionconnectapp.model.StatusEnvio
import br.com.mateushb.extensionconnectapp.model.lerInstanteIso
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

// Fila de envio offline de arquivos e fotos (spec 006, HU-09). Não há backend: o "envio" é simulado
// subindo um progresso até gravar ENVIADO. Regras:
// - o banco guarda só pendente (AGUARDANDO_ENVIO) ou ENVIADO. O item em envio e o progresso ficam em
//   memória ([emAndamento]); "Enviando N%" e "sem internet" são calculados na leitura, pelo
//   ArquivoRepository, com a conectividade. Assim não há uma segunda cópia do estado da rede no banco
//   para ficar fora de sincronia, e cada passo do progresso não grava nem reconsulta o banco;
// - um item por vez, o mais antigo das duas tabelas;
// - se a internet cai no meio (mesmo que volte antes do próximo passo), o item recomeça do zero;
// - app fechado no meio do envio: o item continua pendente e recomeça do zero na próxima abertura.
// Vive no escopo do processo, não de uma tela: iniciada pela MainActivity. Quando houver servidor, só
// enviar() muda
class FilaEnvio private constructor(
    private val arquivoDao: ArquivoDao,
    private val fotoDao: FotoDao,
    private val conectividade: MonitorConectividade
) {

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val iniciada = AtomicBoolean(false)

    private val _emAndamento = MutableStateFlow<EnvioEmAndamento?>(null)
    val emAndamento: StateFlow<EnvioEmAndamento?> = _emAndamento.asStateFlow()

    private sealed interface ItemFila {
        val id: Int
        val dataEnvio: String
        val chave: String

        data class Documento(override val id: Int, override val dataEnvio: String) : ItemFila {
            override val chave: String get() = EnvioEmAndamento.chaveDocumento(id)
        }

        data class Foto(override val id: Int, override val dataEnvio: String) : ItemFila {
            override val chave: String get() = EnvioEmAndamento.chaveFoto(id)
        }
    }

    // Idempotente: a MainActivity chama a cada onCreate (rotação, volta do segundo plano)
    fun iniciar() {
        if (!iniciada.compareAndSet(false, true)) return
        escopo.launch {
            // Bancos gravados antes desta versão da fila ainda podem ter ENVIANDO/progresso salvos
            try {
                arquivoDao.marcarPendentes(StatusEnvio.ENVIANDO.name, StatusEnvio.AGUARDANDO_ENVIO.name, null)
                fotoDao.marcarPendentes(StatusEnvio.ENVIANDO.name, StatusEnvio.AGUARDANDO_ENVIO.name, null)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // A leitura trata qualquer status diferente de ENVIADO como pendente
            }
            processar()
        }
    }

    private suspend fun processar() {
        val pendentes = combine(arquivoDao.observarQuantidadePendentes(), fotoDao.observarQuantidadePendentes()) { a, f -> a + f }
        while (true) {
            try {
                conectividade.conectado.first { it }
                pendentes.first { it > 0 }
                val item = proximo() ?: continue
                enviar(item)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Um item com problema não para a fila; espera um pouco para não girar em falso
                delay(INTERVALO_ERRO_MS)
            }
        }
    }

    // O mais antigo entre as duas tabelas, comparando como Instant
    private suspend fun proximo(): ItemFila? {
        val documento = arquivoDao.buscarProximoPendente()?.let { ItemFila.Documento(it.id, it.dataEnvio) }
        val foto = fotoDao.buscarProximoPendente()?.let { ItemFila.Foto(it.id, it.dataEnvio) }
        return listOfNotNull(documento, foto).minByOrNull { lerInstanteIso(it.dataEnvio) ?: Instant.MIN }
    }

    // Simulação: ~10% a cada 300 ms, só em memória. Qualquer queda de rede desde o início (contada pelo
    // MonitorConectividade) interrompe; o item continua pendente e recomeça do zero quando a rede voltar
    private suspend fun enviar(item: ItemFila) {
        val quedasNoInicio = conectividade.quedas.value
        if (!conectividade.conectado.value) return
        var concluido = false
        try {
            var progresso = 0
            _emAndamento.value = EnvioEmAndamento(item.chave, progresso)
            while (progresso < 100) {
                delay(INTERVALO_PASSO_MS)
                if (!conectividade.conectado.value || conectividade.quedas.value != quedasNoInicio) return
                progresso = minOf(100, progresso + PASSO)
                _emAndamento.value = EnvioEmAndamento(item.chave, progresso)
            }
            // 0 linhas = o item não existe mais (projeto excluído); segue para o próximo
            when (item) {
                is ItemFila.Documento -> arquivoDao.atualizarEnvio(item.id, StatusEnvio.ENVIADO.name, null)
                is ItemFila.Foto -> fotoDao.atualizarEnvio(item.id, StatusEnvio.ENVIADO.name, null)
            }
            concluido = true
        } finally {
            // Concluído, fica em 100% até o próximo item: limpar antes de o Room reemitir a linha como
            // ENVIADO faria o item piscar "Enviando 0%". Interrompido, volta a "esperando a vez"
            if (!concluido) _emAndamento.value = null
        }
    }

    companion object {
        private const val PASSO = 10
        private const val INTERVALO_PASSO_MS = 300L
        private const val INTERVALO_ERRO_MS = 2_000L

        @Volatile
        private var INSTANCE: FilaEnvio? = null

        fun obterInstancia(context: Context): FilaEnvio {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: construir(context).also { INSTANCE = it }
            }
        }

        private fun construir(context: Context): FilaEnvio {
            val banco = AppDatabase.obterInstancia(context)
            return FilaEnvio(banco.arquivoDao(), banco.fotoDao(), MonitorConectividade.obterInstancia(context))
        }
    }
}
