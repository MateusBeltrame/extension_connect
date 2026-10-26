package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Avatar circular com iniciais, frame `avatar` do Figma. O tamanho da fonte acompanha [size]
 * para caber tanto no avatar de lista (40dp) quanto no avatar pequeno de [AvatarStack] (26dp).
 * [containerColor] troca só o fundo (ex.: avatar branco no card de orientação do diário, frame 35:2).
 */
@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral,
    size: Dp = 40.dp,
    containerColor: Color = tone.container
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, color = tone.onContainer, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.35f).sp)
    }
}

/**
 * Pilha de avatares sobrepostos com borda branca, frame `Frame` (grupo de `avatar`) dos
 * cards de projeto. Mostra no máximo [max] avatares; o restante vira um avatar "+N".
 */
@Composable
fun AvatarStack(
    members: List<Pair<String, StatusTone>>,
    modifier: Modifier = Modifier,
    max: Int = 3,
    size: Dp = 26.dp
) {
    val visible = members.take(max)
    val overflow = members.size - visible.size

    Row(modifier) {
        visible.forEachIndexed { index, (initials, tone) ->
            OverlappingAvatar(initials = initials, tone = tone, size = size, index = index)
        }
        if (overflow > 0) {
            OverlappingAvatar(initials = "+$overflow", tone = StatusTone.Neutral, size = size, index = visible.size)
        }
    }
}

@Composable
private fun OverlappingAvatar(initials: String, tone: StatusTone, size: Dp, index: Int) {
    Box(
        modifier = Modifier
            .offset(x = (-6 * index).dp)
            .size(size)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            .background(tone.container),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, color = tone.onContainer, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.35f).sp)
    }
}
