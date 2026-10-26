package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.sp

/**
 * Botão secundário contornado, par do [PrimaryButton] — frame `button` "← Voltar" do rodapé do wizard
 * "Novo projeto" (frames 37:109/37:267): 48dp, fundo branco, borda `outlineVariant`, cantos 8dp,
 * rótulo em negrito `onSurfaceVariant`.
 *
 * [accent] usa borda de 1,5dp e conteúdo rosa, e [compact] a altura de 40dp com rótulo 14sp e ícone
 * 16dp: juntos reproduzem o "Mudar estágio" do card de resumo dos detalhes (frame 34:2).
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    fullWidth: Boolean = true,
    accent: Boolean = false,
    compact: Boolean = false,
    destructive: Boolean = false
) {
    // destructive (spec 008, "Sair da conta" do frame 36:482): borda de 1,5dp e conteúdo vermelhos
    val corDestaque = when {
        destructive -> MaterialTheme.colorScheme.error
        accent -> MaterialTheme.colorScheme.primary
        else -> null
    }
    OutlinedButton(
        onClick = onClick,
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).height(if (compact) 40.dp else 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = if (corDestaque != null) {
            BorderStroke(1.5.dp, corDestaque)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = corDestaque ?: MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp) else ButtonDefaults.ContentPadding
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(if (compact) 16.dp else 18.dp))
            }
            Text(
                text,
                style = if (compact) MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp) else MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}
