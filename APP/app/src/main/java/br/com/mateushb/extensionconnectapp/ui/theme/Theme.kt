package br.com.mateushb.extensionconnectapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrandPink,
    onPrimary = SurfaceWhite,
    background = InkDark,
    surface = SurfaceDark,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextMutedDark,
    outlineVariant = OutlineDark,
    primaryContainer = PinkContainerDark,
    onPrimaryContainer = SurfaceWhite,
    error = ErrorRed,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPink,
    onPrimary = SurfaceWhite,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = InkDark,
    onSurface = InkDark,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = TextMutedLight,
    outlineVariant = OutlineLight,
    primaryContainer = PinkContainerLight,
    onPrimaryContainer = BrandPink,
    error = ErrorRed,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorRed
)

// Dynamic Color (Material You) é intencionalmente desativado: a marca ExtensionConnect usa uma
// paleta fixa (rosa Afya) definida no Figma, que não deve variar com o papel de parede do usuário.
@Composable
fun ExtensionConnectAPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
