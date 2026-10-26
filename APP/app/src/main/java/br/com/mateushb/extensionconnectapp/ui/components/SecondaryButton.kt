package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Botão secundário contornado, par do [PrimaryButton] — frame `button` "← Voltar" do rodapé do wizard
 * "Novo projeto" (frames 37:109/37:267): 48dp, fundo branco, borda `outlineVariant`, cantos 8dp,
 * rótulo em negrito `onSurfaceVariant`.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp)) }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
