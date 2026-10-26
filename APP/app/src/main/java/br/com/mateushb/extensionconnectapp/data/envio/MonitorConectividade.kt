package br.com.mateushb.extensionconnectapp.data.envio

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Há internet agora? Lido pela fila de envio e pelo ArquivoRepository (status exibido dos pendentes).
// Singleton: um único callback registrado no sistema, pela vida do processo
class MonitorConectividade private constructor(context: Context) {

    private val gerenciador = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    private val _conectado = MutableStateFlow(temInternet(gerenciador.getNetworkCapabilities(gerenciador.activeNetwork)))
    val conectado: StateFlow<Boolean> = _conectado.asStateFlow()

    // Quantas vezes a internet caiu. A fila compara antes e depois de cada passo: uma queda que volta
    // entre dois passos também faz o envio recomeçar do zero (spec 006, HU-09)
    private val _quedas = MutableStateFlow(0)
    val quedas: StateFlow<Int> = _quedas.asStateFlow()

    // Rede padrão atual. Na troca Wi-Fi → dados móveis, o onLost atrasado do Wi-Fi não pode marcar o
    // aparelho como sem internet enquanto os dados móveis já são a rede padrão
    @Volatile
    private var redeAtual: Network? = gerenciador.activeNetwork

    init {
        gerenciador.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
                redeAtual = network
                atualizar(temInternet(capacidades))
            }

            override fun onLost(network: Network) {
                if (network != redeAtual) return
                redeAtual = null
                atualizar(false)
            }
        })
    }

    private fun atualizar(conectado: Boolean) {
        if (_conectado.value && !conectado) _quedas.update { it + 1 }
        _conectado.value = conectado
    }

    private fun temInternet(capacidades: NetworkCapabilities?): Boolean =
        capacidades?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    companion object {
        @Volatile
        private var INSTANCE: MonitorConectividade? = null

        fun obterInstancia(context: Context): MonitorConectividade {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MonitorConectividade(context).also { INSTANCE = it }
            }
        }
    }
}
