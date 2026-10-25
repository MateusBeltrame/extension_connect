package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cartão branco arredondado com sombra suave, usado em `academic-card`, `card` e `card-projeto`
 * do Figma. Encapsula apenas o contêiner — o conteúdo (título, chips, avatares...) é montado
 * no local de uso a partir dos demais componentes de `ui/components/`. Com [onClick], o cartão inteiro
 * é clicável (ripple respeita os cantos arredondados).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = elevation) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}
