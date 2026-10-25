package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.ui.components.BrandLogo
import br.com.mateushb.extensionconnectapp.ui.components.GradientHeader

// Estrutura comum das telas de acesso (frames T1 do Figma): cabeçalho de marca por trás da status
// bar + coluna rolável com o card e os links de rodapé (padding 20dp, espaçamento 20dp)
@Composable
internal fun AcessoLayout(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navigationIcon: (@Composable () -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        // Sem insets no Scaffold (o cabeçalho fica atrás da status bar), então o Snackbar desvia
        // sozinho da barra de navegação e do teclado
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding().imePadding()) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // Insets ANTES do scroll: encolhem a área visível, então o campo focado é rolado para
                // cima do teclado (edge-to-edge não redimensiona mais a janela)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            GradientHeader(
                title = "ExtensionConnect",
                subtitle = "Gestão Acadêmica de Extensão",
                navigationIcon = navigationIcon,
                logo = { BrandLogo() }
            )
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = horizontalAlignment,
                content = content
            )
        }
    }
}

@Composable
internal fun CabecalhoCard(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = titulo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(text = subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun IconeCampo(icone: ImageVector) {
    Icon(imageVector = icone, contentDescription = null, modifier = Modifier.size(18.dp))
}
