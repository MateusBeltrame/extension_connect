package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.FotoDetalhes
import br.com.mateushb.extensionconnectapp.ui.components.MiniaturaFoto
import java.time.LocalDate

// Sheet da foto (spec 006, HU-07): foto maior, autorização do Mural e excluir. Só o autor, com projeto
// não finalizado ([podeGerenciar]), muda a autorização e exclui. Ligar a autorização pede o
// consentimento num AlertDialog; desligar é direto. O Switch reflete o banco (sem mudança otimista)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FotoBottomSheet(
    detalhes: FotoDetalhes,
    hoje: LocalDate,
    podeGerenciar: Boolean,
    onAlterarAutorizacao: (autorizada: Boolean) -> Unit,
    onExcluir: () -> Unit,
    onDispensar: () -> Unit
) {
    val foto = detalhes.foto
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmarAutorizacao by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDispensar,
        sheetState = sheetState,
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
            // Altura pela proporção da foto; sem arquivo no aparelho, o gradiente ocupa um quadrado
            MiniaturaFoto(
                caminho = foto.caminhoLocal,
                semente = foto.id,
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 420.dp),
                cantos = 12.dp,
                tamanhoIcone = 40.dp,
                ladoMaximoPx = 1080,
                proporcaoOriginal = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = foto.nomeArquivo,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detalhes.legendaExibida(hoje),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Autorizada para divulgação no Mural",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = foto.autorizadaMural,
                    onCheckedChange = { ligar -> if (ligar) confirmarAutorizacao = true else onAlterarAutorizacao(false) },
                    enabled = podeGerenciar,
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }

            if (podeGerenciar) {
                TextButton(onClick = { confirmarExclusao = true }) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Excluir foto",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }

    if (confirmarAutorizacao) {
        AlertDialog(
            onDismissRequest = { confirmarAutorizacao = false },
            title = { Text("Autorizar divulgação") },
            text = { Text(TEXTO_CONSENTIMENTO_FOTO) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarAutorizacao = false
                    onAlterarAutorizacao(true)
                }) {
                    Text("Autorizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarAutorizacao = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (confirmarExclusao) {
        ConfirmacaoExclusaoArquivoDialog(
            texto = "Deseja realmente excluir esta foto?",
            onConfirmar = {
                confirmarExclusao = false
                onExcluir()
            },
            onDispensar = { confirmarExclusao = false }
        )
    }
}
