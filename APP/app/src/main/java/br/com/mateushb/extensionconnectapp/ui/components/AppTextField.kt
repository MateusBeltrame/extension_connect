package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber

/**
 * Campo de texto padrão do app — frame `input`/`input-wrapper` do Figma (44dp, borda 1dp, cantos
 * 8dp, ícones 18dp a 12dp das bordas). Serve tanto para campos de formulário com rótulo
 * (`field-E-mail`, `field-Senha`...) quanto para campos de busca sem rótulo (`field-campo`):
 * basta omitir [label].
 *
 * [supportingText] é o texto abaixo do campo: ajuda em cinza ("Mínimo de 6 caracteres") ou, com
 * [isError], a mensagem de validação em vermelho. Com [onClick] o campo vira um seletor somente
 * leitura (ex.: dropdown de curso) — o toque em qualquer ponto chama [onClick].
 *
 * Wizard "Novo projeto" (spec 003): [counter] ("12/50") fica à direita, abaixo do campo; [height]
 * maior com `singleLine = false` faz a área de várias linhas (Descrição, 110dp), com o texto no topo;
 * [isWarning] usa a borda laranja do aviso de data (frame 37:109); `enabled = false` é a busca
 * bloqueada no limite de participantes (fundo cinza, frame 37:393).
 *
 * [locked] (spec 007): campo que a regra não deixa editar — o texto também fica cinza (e-mail e perfil
 * do próprio cadastro em "Editar pessoa", frames 36:175 e 36:420). Só `enabled = false` (ex.: enquanto
 * salva) mantém o texto escuro, para não parecer que o valor sumiu.
 *
 * [supportingIcon] (spec 008): ícone de 14dp antes do [supportingText], a 6dp dele, com o texto em
 * Medium ("⚠ Senha atual incorreta", frame 36:754).
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    height: Dp = 44.dp,
    counter: String? = null,
    isWarning: Boolean = false,
    locked: Boolean = false,
    supportingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focado by interactionSource.collectIsFocusedAsState()
    val corBorda = when {
        isError -> MaterialTheme.colorScheme.error
        isWarning -> WarningAmber
        focado -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Column(modifier) {
        if (label != null) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled && onClick == null,
            readOnly = onClick != null,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (locked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { campo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .background(
                            if (enabled && !locked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, corBorda, RoundedCornerShape(8.dp))
                        .then(if (onClick != null && enabled && !locked) Modifier.clickable(onClick = onClick) else Modifier)
                        .padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 12.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                        leadingIcon?.invoke()
                        Box(
                            Modifier.weight(1f),
                            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                        ) {
                            if (value.isEmpty()) {
                                Text(text = placeholder, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                            }
                            campo()
                        }
                        trailingIcon?.invoke()
                    }
                }
            }
        )
        if (supportingText != null || counter != null) {
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(if (supportingIcon != null) 6.dp else 8.dp)) {
                val corApoio = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                if (supportingIcon != null && supportingText != null) {
                    Icon(
                        imageVector = supportingIcon,
                        contentDescription = null,
                        tint = corApoio,
                        modifier = Modifier.padding(top = 1.dp).size(14.dp)
                    )
                }
                Text(
                    text = supportingText.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = if (supportingIcon != null) {
                            FontWeight.Medium
                        } else {
                            MaterialTheme.typography.bodySmall.fontWeight
                        }
                    ),
                    color = corApoio,
                    modifier = Modifier.weight(1f)
                )
                if (counter != null) {
                    Text(
                        text = counter,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
