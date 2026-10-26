package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Selo (pill) tonal somente leitura, para status e categorias — frame `chip` do Figma
 * (ex.: "Em análise", "Finalizado", papéis "Aluno"/"Professor"/"Administrador").
 * Para chips selecionáveis (filtros), usar o `FilterChip` do Material 3; este componente é
 * só para rótulos informativos, sem estado de seleção. [containerColor] e [contentColor] trocam o
 * fundo e o texto (ex.: chip de etapa sobre `surface` no card rosa de orientação do diário, frame 35:2).
 */
@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral,
    leadingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = tone.container,
    contentColor: Color = tone.onContainer
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        leadingIcon?.invoke()
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp)
        )
    }
}
