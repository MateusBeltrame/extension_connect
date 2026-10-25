package br.com.mateushb.extensionconnectapp.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppPreferencesManager(context: Context) {

    companion object {
        private const val PREFS_FILE = "extension_connect_preferences"
        private const val KEY_USUARIO_LOGADO_ID = "pref_usuario_logado_id"
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
}
