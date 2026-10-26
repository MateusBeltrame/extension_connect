package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.ui.theme.BrandPink
import br.com.mateushb.extensionconnectapp.ui.theme.InkDark

/**
 * Barra de título das telas internas (Projetos, Pessoas, Mural...). Reproduz o frame `top-bar`
 * do Figma: mesmo gradiente do [GradientHeader], porém compacta, com título + subtítulo
 * opcional à esquerda e até uma ação à direita (ex.: filtro, busca). O gradiente se estende por trás
 * da status bar (edge-to-edge), como no [GradientHeader]. [navigationIcon] (ex.: X do wizard "Novo
 * projeto", frame 37:2) recebe um `IconButton` de 48dp: o ícone fica a 20dp da borda e o título logo
 * depois, como no Figma. [compactTitle] usa o título de 16sp em até 2 linhas, para o título do
 * projeto na aba Diário (frame 35:2).
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    compactTitle: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(BrandPink, InkDark)))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = if (navigationIcon != null) 8.dp else 20.dp, end = 20.dp, top = 6.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ícone e título juntos, sem o espaço de 12dp: o IconButton (48dp) já traz o respiro
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (navigationIcon != null) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    navigationIcon()
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (compactTitle) 16.sp else 20.sp,
                        lineHeight = if (compactTitle) 20.sp else MaterialTheme.typography.titleLarge.lineHeight,
                        fontWeight = FontWeight.Bold
                    ),
                    // Título dinâmico (ex.: título do projeto, até 100 caracteres) não quebra a barra
                    maxLines = if (compactTitle) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
        actions()
    }
}
