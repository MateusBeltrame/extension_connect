package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.BottomLink
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.state.RecuperarSenhaUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

@Composable
fun RecuperarSenhaScreen(
    recuperarSenhaUiState: RecuperarSenhaUiState,
    onEnviarClick: (email: String) -> Unit,
    onVoltarClick: () -> Unit
) {
    AcessoLayout(
        navigationIcon = {
            IconButton(onClick = onVoltarClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (recuperarSenhaUiState is RecuperarSenhaUiState.Enviado) {
            LinkEnviadoConteudo(onVoltarClick = onVoltarClick)
        } else {
            FormularioRecuperarSenha(onEnviarClick = onEnviarClick)
            BottomLink(prefix = "Lembrou a senha?", action = "Voltar ao login", onClick = onVoltarClick)
        }
    }
}

@Composable
private fun FormularioRecuperarSenha(onEnviarClick: (String) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailErro by remember { mutableStateOf<String?>(null) }

    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            CabecalhoCard(
                titulo = "Recuperar senha",
                subtitulo = "Informe seu e-mail e enviaremos um link para você criar uma nova senha."
            )
            AppTextField(
                value = email,
                onValueChange = { email = it; emailErro = null },
                placeholder = "usuario@afya.br",
                label = "E-mail",
                leadingIcon = { IconeCampo(Icons.Outlined.Email) },
                supportingText = emailErro,
                isError = emailErro != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
            )
            PrimaryButton(
                text = "Enviar link",
                onClick = {
                    emailErro = ValidacaoAcesso.erroEmail(email)
                    if (emailErro == null) onEnviarClick(email)
                }
            )
        }
    }
}

// Confirmação neutra (frame 32:284): não revela se o e-mail está cadastrado
@Composable
private fun LinkEnviadoConteudo(onVoltarClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Verifique seu e-mail",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Se o e-mail estiver cadastrado, você receberá um link para criar uma nova senha. " +
                    "Confira também a caixa de spam.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = onVoltarClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Voltar ao login", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun RecuperarSenhaScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        RecuperarSenhaScreen(
            recuperarSenhaUiState = RecuperarSenhaUiState.Enviado,
            onEnviarClick = {},
            onVoltarClick = {}
        )
    }
}
