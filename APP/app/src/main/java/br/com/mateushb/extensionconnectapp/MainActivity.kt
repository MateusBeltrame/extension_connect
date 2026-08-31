package br.com.mateushb.extensionconnectapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.mateushb.extensionconnectapp.login.Login
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExtensionConnectAPPTheme(darkTheme = false, dynamicColor = false) {
                Login()
            }
        }
    }
}
