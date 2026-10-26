package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.ui.theme.BrandPink
import br.com.mateushb.extensionconnectapp.ui.theme.InkDark

/**
 * Cabeçalho de marca usado nas telas de acesso (login, cadastro, recuperar senha...).
 * Reproduz o frame `gradient-header`/`header-branding` do Figma: gradiente rosa→preto,
 * marca centralizada (logo + título + selo em caixa alta). O gradiente se estende por trás da
 * status bar (edge-to-edge); [navigationIcon] (ex.: seta de voltar em "Recuperar senha") fica no
 * canto superior esquerdo, alinhado ao topo do logo.
 *
 * "Meu perfil" (spec 008, frame `36:483`): [compact] reduz os respiros (8dp entre logo e título) e usa
 * título de 20sp Bold; [badge] é um slot abaixo do título (chip do perfil), no lugar do [subtitle].
 */
@Composable
fun GradientHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    compact: Boolean = false,
    badge: (@Composable () -> Unit)? = null,
    logo: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(BrandPink, InkDark)))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (compact) 16.dp else 12.dp,
                    bottom = if (compact) 24.dp else 28.dp,
                    start = if (compact) 20.dp else 24.dp,
                    end = if (compact) 20.dp else 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            logo()
            Spacer(Modifier.height(if (compact) 8.dp else 16.dp))
            Text(
                text = title,
                color = Color.White,
                style = if (compact) {
                    MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                textAlign = TextAlign.Center
            )
            if (badge != null) {
                Spacer(Modifier.height(8.dp))
                badge()
            }
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle.uppercase(),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (navigationIcon != null) {
            Box(Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 4.dp)) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    navigationIcon()
                }
            }
        }
    }
}
