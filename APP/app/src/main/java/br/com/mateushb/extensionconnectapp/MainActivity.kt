package br.com.mateushb.extensionconnectapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.mateushb.extensionconnectapp.data.envio.FilaEnvio
import br.com.mateushb.extensionconnectapp.ui.navigation.AppNavHost
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Fila de envio de arquivos e fotos (spec 006): retoma o que ficou pendente, com qualquer tela aberta
        FilaEnvio.obterInstancia(this).iniciar()
        setContent {
            ExtensionConnectAPPTheme(darkTheme = false) {
                AppNavHost()
            }
        }
    }
}
