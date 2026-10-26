package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.BrandLogo
import br.com.mateushb.extensionconnectapp.ui.components.GradientHeader
import br.com.mateushb.extensionconnectapp.ui.theme.SuccessGreen

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

// Campo de senha com olho para mostrar/ocultar e check verde quando a confirmação confere (telas de
// acesso e "Trocar senha", spec 008)
@Composable
internal fun CampoSenha(
    valor: String,
    onValorChange: (String) -> Unit,
    rotulo: String,
    visivel: Boolean,
    onAlternarVisibilidade: () -> Unit,
    erro: String?,
    imeAction: ImeAction,
    ajuda: String? = null,
    confirmado: Boolean = false,
    destacarErro: Boolean = false,
    enabled: Boolean = true
) {
    AppTextField(
        value = valor,
        onValueChange = onValorChange,
        placeholder = "••••••••",
        label = rotulo,
        // destacarErro (spec 008, frame 15b): cadeado vermelho e ícone antes da mensagem de erro
        leadingIcon = {
            if (destacarErro && erro != null) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                IconeCampo(Icons.Outlined.Lock)
            }
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Área de toque de 32dp; o offset mantém o ícone a 12dp da borda como no Figma
                Box(
                    modifier = Modifier
                        .offset(x = if (confirmado) 0.dp else 7.dp)
                        .size(32.dp)
                        .clickable(onClick = onAlternarVisibilidade),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (visivel) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (visivel) "Ocultar senha" else "Mostrar senha",
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (confirmado) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Senhas conferem",
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        visualTransformation = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
        supportingText = erro ?: ajuda,
        isError = erro != null,
        supportingIcon = if (destacarErro && erro != null) Icons.Outlined.ErrorOutline else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        enabled = enabled
    )
}
