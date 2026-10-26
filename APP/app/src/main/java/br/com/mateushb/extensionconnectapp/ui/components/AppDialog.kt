package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Diálogo com ícone em círculo tonal, título e texto alinhados à esquerda — frame `dialog` do Figma
 * (36:254 "Desativar {nome}?" e 36:348 "Troque o orientador antes"): cartão branco de cantos 16dp,
 * respiro de 24dp, ícone 22dp num círculo de 48dp, título 18sp em negrito, texto 14sp e botões de 44dp.
 *
 * Com [dismissText] mostra dois botões lado a lado ([SecondaryButton] + [PrimaryButton]); sem ele, só
 * o botão de confirmação em largura total ("Entendi"). [destructive] pinta a confirmação de vermelho.
 * Tocar fora ou voltar chama [onDismiss]. O véu escuro atrás é o scrim padrão do `Dialog`.
 */
@Composable
fun AppDialog(
    icon: ImageVector,
    iconColor: Color,
    iconContainerColor: Color,
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String? = null,
    destructive: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Altura de 44dp do Figma: o modificador externo prevalece sobre os 48dp dos botões
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (dismissText != null) {
                        SecondaryButton(text = dismissText, onClick = onDismiss, modifier = Modifier.weight(1f).height(44.dp))
                    }
                    PrimaryButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(44.dp),
                        destructive = destructive
                    )
                }
            }
        }
    }
}
