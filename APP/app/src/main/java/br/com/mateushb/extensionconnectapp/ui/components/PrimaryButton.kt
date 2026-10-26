package br.com.mateushb.extensionconnectapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Botão de ação principal (CTA) usado em todas as telas: "Criar Conta", "Acessar Portal"...
 * Reproduz o frame `primary-button` do Figma: largura total, 48dp, fundo rosa, rótulo branco em negrito.
 * Com [loading] mostra um indicador de progresso no lugar do rótulo, mantém a cor da marca e
 * ignora toques. [leadingIcon] (18dp) e `fullWidth = false` reproduzem o botão "+ Criar projeto" do
 * estado vazio de Projetos (frame 33:200), que acompanha a largura do conteúdo. [trailingIcon] é a
 * seta de "Avançar →" do wizard "Novo projeto" (frame 37:2). [compact] é a variante de 40dp dos
 * botões lado a lado do card de resumo dos detalhes ("Confirmar ciência", frame 34:2): rótulo 14sp,
 * ícone 16dp e respiro lateral de 12dp, para dois botões caberem na largura do card. [destructive]
 * troca o rosa pelo vermelho de `error` ("Desativar" do diálogo 10b, frame 36:254).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    fullWidth: Boolean = true,
    trailingIcon: ImageVector? = null,
    compact: Boolean = false,
    destructive: Boolean = false
) {
    val corFundo = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val tamanhoIcone = if (compact) 16.dp else 18.dp
    Button(
        onClick = onClick,
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).height(if (compact) 40.dp else 48.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = corFundo,
            disabledContainerColor = if (loading) corFundo else MaterialTheme.colorScheme.outlineVariant
        ),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp) else ButtonDefaults.ContentPadding
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 20.dp else 24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = if (fullWidth) 0.dp else 4.dp),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(tamanhoIcone)) }
                Text(
                    text,
                    style = if (compact) MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp) else MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
                trailingIcon?.let { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(tamanhoIcone)) }
            }
        }
    }
}
