package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Aviso inline de erro (fundo vermelho claro, ícone + texto vermelho) — frame do erro de login
 * (`32:59`) e da conta desativada (`32:117`) no Figma. Para erros de validação de um campo use o
 * `supportingText` do [AppTextField]; este banner é para o resultado de uma ação do formulário.
 *
 * Com [containerColor]/[contentColor] e [title] vira também o aviso informativo dos detalhes do
 * projeto: azul "Projeto finalizado: somente leitura" (frame 34:220) e amarelo "Ainda há N etapas
 * pendentes" (frame 33:988). Nesses casos o ícone é de 18dp, como no Figma.
 */
@Composable
fun AlertBanner(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.errorContainer,
    contentColor: Color = MaterialTheme.colorScheme.onErrorContainer,
    iconSize: Dp = 16.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(if (title != null) 10.dp else 8.dp))
            .padding(horizontal = if (title != null) 14.dp else 12.dp, vertical = if (title != null) 12.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(if (title != null) 10.dp else 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Text(
                text = message,
                style = if (title != null) MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp) else MaterialTheme.typography.bodyMedium,
                fontWeight = if (title != null) FontWeight.Normal else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
