package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.FotoPreparada
import br.com.mateushb.extensionconnectapp.model.formatarTamanho
import br.com.mateushb.extensionconnectapp.ui.components.AlertBanner
import br.com.mateushb.extensionconnectapp.ui.components.MiniaturaFoto
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SecondaryButton
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber
import br.com.mateushb.extensionconnectapp.ui.theme.WarningContainerLight
import kotlinx.coroutines.launch

internal const val TEXTO_CONSENTIMENTO_FOTO =
    "Confirme que todas as pessoas da foto autorizaram o uso da imagem. Fotos com crianças e adolescentes " +
        "exigem autorização dos responsáveis."

// Sheet "Autorizar divulgação" (spec 006, HU-06, frame 35:630). Abre sem opção marcada: a autorização é
// decisão explícita. [foto] nulo = ainda comprimindo (prévia com indicador, envio desabilitado). Como o
// NovoRegistroBottomSheet, não fecha por gesto enquanto grava, e o erro de gravação [erro] aparece aqui dentro
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AutorizarDivulgacaoBottomSheet(
    foto: FotoPreparada?,
    enviando: Boolean,
    erro: String?,
    onEnviar: (autorizada: Boolean) -> Unit,
    onDispensar: () -> Unit
) {
    val enviandoAtual by rememberUpdatedState(enviando)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !enviandoAtual }
    )
    val escopo = rememberCoroutineScope()

    // Saveable: a escolha sobrevive à rotação e ao erro de gravação
    var autorizada by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var tentouEnviar by rememberSaveable { mutableStateOf(false) }
    val erroEscolha = "Escolha se a foto pode aparecer no Mural.".takeIf { tentouEnviar && autorizada == null }

    ModalBottomSheet(
        onDismissRequest = { if (!enviandoAtual) onDispensar() },
        sheetState = sheetState,
        sheetGesturesEnabled = !enviando,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { AlcaBottomSheet() }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    MiniaturaFoto(
                        caminho = foto?.caminhoTemporario.orEmpty(),
                        semente = 1,
                        modifier = Modifier.size(72.dp),
                        cantos = 10.dp,
                        tamanhoIcone = if (foto == null) 0.dp else 28.dp,
                        ladoMaximoPx = 216
                    )
                    if (foto == null) CircularProgressIndicator(color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(28.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = foto?.nomeArquivo ?: "Preparando foto…",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (foto != null) {
                        Text(
                            text = "Foto comprimida · ${formatarTamanho(foto.tamanhoBytes)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Text(
                text = "Autorizada para divulgação no Mural?",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OpcaoAutorizacao(
                    titulo = "Não",
                    descricao = "A foto fica visível só para quem participa do projeto.",
                    selecionada = autorizada == false,
                    enabled = !enviando,
                    onClick = { autorizada = false }
                )
                OpcaoAutorizacao(
                    titulo = "Sim",
                    descricao = "A foto poderá aparecer no Mural público quando o projeto for finalizado.",
                    selecionada = autorizada == true,
                    enabled = !enviando,
                    onClick = { autorizada = true }
                )
            }

            FaixaAviso(
                texto = TEXTO_CONSENTIMENTO_FOTO,
                icone = Icons.Outlined.WarningAmber,
                containerColor = WarningContainerLight,
                contentColor = WarningAmber,
                tamanhoIcone = 18.dp,
                fontWeight = FontWeight.Medium
            )

            (erroEscolha ?: erro)?.let { AlertBanner(message = it, icon = Icons.Outlined.ErrorOutline) }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Cancelar",
                    onClick = { escopo.launch { sheetState.hide() }.invokeOnCompletion { onDispensar() } },
                    enabled = !enviando,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Enviar foto",
                    onClick = {
                        tentouEnviar = true
                        autorizada?.let(onEnviar)
                    },
                    enabled = foto != null,
                    loading = enviando,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Cartão de rádio: marcado com borda rosa de 1,5dp e fundo rosa bem claro (#FFF7FB ≈ primary a 3%)
@Composable
private fun OpcaoAutorizacao(
    titulo: String,
    descricao: String,
    selecionada: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(10.dp)
    val primaria = MaterialTheme.colorScheme.primary
    val borda = if (selecionada) BorderStroke(1.5.dp, primaria) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (selecionada) primaria.copy(alpha = 0.03f) else MaterialTheme.colorScheme.surface)
            .border(borda, forma)
            .selectable(selected = selecionada, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Círculo de 20dp do Figma: contorno cinza de 2dp; marcado, contorno e miolo rosa
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(2.dp, if (selecionada) primaria else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selecionada) Box(Modifier.size(10.dp).background(primaria, CircleShape))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = descricao,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
