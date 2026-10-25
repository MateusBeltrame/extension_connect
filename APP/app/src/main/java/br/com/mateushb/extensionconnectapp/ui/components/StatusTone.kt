package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.ui.graphics.Color

// Pares (fundo tonal, conteúdo) extraídos dos chips e avatares do Figma; compartilhados por
// StatusChip e Avatar para manter a mesma linguagem visual de "status"/"categoria" nas duas telas.
enum class StatusTone(val container: Color, val onContainer: Color) {
    Neutral(Color(0xFFEEF0F3), Color(0xFF4B5563)),
    Pink(Color(0xFFFDE7F3), Color(0xFFE6007E)),
    Blue(Color(0xFFE3F2FD), Color(0xFF1565C0)),
    Green(Color(0xFFE8F5E9), Color(0xFF2E7D32)),
    Amber(Color(0xFFFFF3E0), Color(0xFFB26A00)),
    Red(Color(0xFFFDECEA), Color(0xFFC62828)),
    Purple(Color(0xFFF3E8FF), Color(0xFF6B21A8)),
    Orange(Color(0xFFFFF7ED), Color(0xFFC2410C))
}
