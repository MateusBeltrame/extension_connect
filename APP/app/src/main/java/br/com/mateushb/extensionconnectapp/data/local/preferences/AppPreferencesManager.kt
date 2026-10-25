package br.com.mateushb.extensionconnectapp.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.FiltroProjetos
import br.com.mateushb.extensionconnectapp.model.TemaProjeto

class AppPreferencesManager(context: Context) {

    companion object {
        private const val PREFS_FILE = "extension_connect_preferences"
        private const val KEY_USUARIO_LOGADO_ID = "pref_usuario_logado_id"
        private const val KEY_FILTRO_PROJETOS_ESTAGIO = "pref_filtro_projetos_estagio"
        private const val KEY_FILTRO_PROJETOS_TEMA = "pref_filtro_projetos_tema"
        private const val KEY_FILTRO_PROJETOS_ORIENTADOR_ID = "pref_filtro_projetos_orientador_id"
        private const val KEY_FILTRO_PROJETOS_SOU_ORIENTADOR = "pref_filtro_projetos_sou_orientador"
        private const val KEY_FILTRO_PROJETOS_SO_PARADOS = "pref_filtro_projetos_so_parados"
        private const val SEM_USUARIO = -1
    }

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    // Sessão sempre lembrada até "Sair da conta" (spec 001)
    fun salvarUsuarioLogadoId(id: Int) {
        sharedPreferences.edit { putInt(KEY_USUARIO_LOGADO_ID, id) }
    }

    fun obterUsuarioLogadoId(): Int? {
        val id = sharedPreferences.getInt(KEY_USUARIO_LOGADO_ID, SEM_USUARIO)
        return if (id == SEM_USUARIO) null else id
    }

    fun limparSessao() {
        sharedPreferences.edit { remove(KEY_USUARIO_LOGADO_ID) }
    }

    // Filtros da tela Projetos (spec 002); enums gravados pelo name, ausência = sem filtro
    fun salvarFiltroProjetos(filtro: FiltroProjetos) {
        sharedPreferences.edit {
            putString(KEY_FILTRO_PROJETOS_ESTAGIO, filtro.estagio?.name)
            putString(KEY_FILTRO_PROJETOS_TEMA, filtro.tema?.name)
            putInt(KEY_FILTRO_PROJETOS_ORIENTADOR_ID, filtro.orientadorId ?: SEM_USUARIO)
            putBoolean(KEY_FILTRO_PROJETOS_SOU_ORIENTADOR, filtro.souOrientador)
            putBoolean(KEY_FILTRO_PROJETOS_SO_PARADOS, filtro.soParados)
        }
    }

    // Valor inválido (ex.: enum renomeado) cai no padrão "sem filtro"
    fun obterFiltroProjetos(): FiltroProjetos {
        val estagio = sharedPreferences.getString(KEY_FILTRO_PROJETOS_ESTAGIO, null)
        val tema = sharedPreferences.getString(KEY_FILTRO_PROJETOS_TEMA, null)
        val orientadorId = sharedPreferences.getInt(KEY_FILTRO_PROJETOS_ORIENTADOR_ID, SEM_USUARIO)
        return FiltroProjetos(
            estagio = EstagioProjeto.entries.firstOrNull { it.name == estagio },
            tema = TemaProjeto.entries.firstOrNull { it.name == tema },
            orientadorId = if (orientadorId == SEM_USUARIO) null else orientadorId,
            souOrientador = sharedPreferences.getBoolean(KEY_FILTRO_PROJETOS_SOU_ORIENTADOR, false),
            soParados = sharedPreferences.getBoolean(KEY_FILTRO_PROJETOS_SO_PARADOS, false)
        )
    }
}
