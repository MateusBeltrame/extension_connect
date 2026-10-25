package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Botão de ação principal (CTA) usado em todas as telas: "Criar Conta", "Acessar Portal"...
 * Reproduz o frame `primary-button` do Figma: largura total, 48dp, fundo rosa, rótulo branco em negrito.
 * Com [loading] mostra um indicador de progresso no lugar do rótulo, mantém a cor da marca e
 * ignora toques. [leadingIcon] (18dp) e `fullWidth = false` reproduzem o botão "+ Criar projeto" do
 * estado vazio de Projetos (frame 33:200), que acompanha a largura do conteúdo.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).height(48.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = if (loading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = if (fullWidth) 0.dp else 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp)) }
                Text(text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
