package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.R

/**
 * Logo do ExtensionConnect (capelo + rede) — frame `logo-container` do Figma, recortado em
 * círculo. Usado no slot `logo` do [GradientHeader].
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Image(
        painter = painterResource(R.drawable.logo_extension_connect),
        contentDescription = "Logo ExtensionConnect",
        modifier = modifier.size(size).clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
