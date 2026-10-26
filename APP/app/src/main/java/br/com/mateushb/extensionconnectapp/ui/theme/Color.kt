package br.com.mateushb.extensionconnectapp.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta extraída do design Figma "register-screen" (arquivo K1TIrDoqzAxYtJIaTJJuMt)

val BrandPink = Color(0xFFE6007E)
val InkDark = Color(0xFF121212)
val BackgroundLight = Color(0xFFF8F9FA)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextSecondaryLight = Color(0xFF4B5563)
val TextMutedLight = Color(0xFF9CA3AF)
val OutlineLight = Color(0xFFE5E7EB)
val NeutralContainerLight = Color(0xFFEEF0F3) // trilha das abas e colunas do quadro (frames 33:2, 33:279)
val PinkContainerLight = Color(0xFFFDE7F3) // círculo do ícone em "Verifique seu e-mail" (frame 32:284)
val ErrorRed = Color(0xFFDC2626) // texto/ícone do aviso de erro (frames 32:59 e 32:117)
val ErrorContainerLight = Color(0xFFFEF2F2) // fundo do aviso de erro
val SuccessGreen = Color(0xFF10B981) // check de confirmação de senha (frame 32:176)
val WarningAmber = Color(0xFFB45309) // data de etapa fora do período e "Limite atingido" (frames 37:109, 37:393)
val WarningContainerLight = Color(0xFFFFFBEB) // fundo do contador de participantes no limite (frame 37:393)
val WarningOutlineLight = Color(0xFFFCD34D) // borda do contador de participantes no limite
val InfoContainerLight = Color(0xFFEFF6FF) // fundo do aviso "Projeto finalizado: somente leitura" (frame 34:220)

// O Figma não define uma variante escura; valores abaixo preservam a marca (rosa + fundo quase preto
// já usado no gradiente do cabeçalho) e seguem a escala neutra padrão do Material 3 para contraste.
val SurfaceDark = Color(0xFF1E1E1E)
val TextSecondaryDark = Color(0xFF9CA3AF)
val TextMutedDark = Color(0xFF6B7280)
val OutlineDark = Color(0xFF3A3A3A)
val PinkContainerDark = Color(0xFF4A1030)
val ErrorContainerDark = Color(0xFF3B1414)

// Aba Arquivos (spec 006, frame 35:231)
val InfoBlue = Color(0xFF2563EB) // selo DOCX (fundo: InfoContainerLight)
val SuccessContainerLight = Color(0xFFF0FDF4) // fundo do selo XLSX
val SuccessGreenStrong = Color(0xFF16A34A) // selo XLSX

// Gradientes (135°, cor clara → escura em 50%) das fotos do Figma: placeholder da miniatura enquanto a
// imagem carrega ou quando o arquivo não existe no aparelho
val GradientesFoto: List<Pair<Color, Color>> = listOf(
    Color(0xFFA7D7A0) to Color(0xFF5E9E58),
    Color(0xFFF7C59F) to Color(0xFFE07A3E),
    Color(0xFF9DC3E6) to Color(0xFF3D7FB8),
    Color(0xFFC9E4A5) to Color(0xFF7DAA45),
    Color(0xFFF4D58D) to Color(0xFFC99A2E),
    Color(0xFFD7B8E8) to Color(0xFF8F5BB0)
)
