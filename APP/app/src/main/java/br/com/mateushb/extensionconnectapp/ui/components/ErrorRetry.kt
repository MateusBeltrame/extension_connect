package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Erro de carregamento com "Tentar novamente" (Artigo 5.6 da constituição): mensagem em
 * `colorScheme.error` centralizada + botão contornado. Usado na lista de Projetos, no wizard
 * "Novo projeto" e nos detalhes do projeto; passe `Modifier.fillMaxSize()` para centralizar na tela.
 * [actionLabel] troca o rótulo quando repetir não resolve ("Voltar" no projeto não encontrado).
 */
@Composable
fun ErrorRetry(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Tentar novamente"
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry) {
            Text(actionLabel)
        }
    }
}
