package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Sheet "Enviar arquivo" da aba Arquivos (spec 006, HU-05, frame 35:408). Escolher uma opção fecha o
// sheet com a animação e só então abre a câmera ou o seletor do sistema
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EnviarArquivoBottomSheet(
    onTirarFoto: () -> Unit,
    onEscolherFoto: () -> Unit,
    onEscolherDocumento: () -> Unit,
    onDispensar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val escopo = rememberCoroutineScope()
    val fecharE: (() -> Unit) -> Unit = { acao ->
        escopo.launch { sheetState.hide() }.invokeOnCompletion {
            onDispensar()
            acao()
        }
    }

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
            Text(
                text = "Enviar arquivo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            OpcaoEnvio(
                icone = Icons.Outlined.PhotoCamera,
                titulo = "Tirar foto",
                descricao = "Abre a câmera do celular",
                onClick = { fecharE(onTirarFoto) }
            )
            OpcaoEnvio(
                icone = Icons.Outlined.Image,
                titulo = "Escolher foto",
                descricao = "Da galeria do celular",
                onClick = { fecharE(onEscolherFoto) }
            )
            OpcaoEnvio(
                icone = Icons.Outlined.Description,
                titulo = "Escolher documento",
                descricao = "PDF, DOCX, TXT ou XLSX",
                onClick = { fecharE(onEscolherDocumento) }
            )
            FaixaAviso(
                texto = "Formatos aceitos: JPG, PNG, HEIC, PDF, DOCX, TXT e XLSX, até 10 MB.",
                icone = Icons.Outlined.Info,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tamanhoIcone = 16.dp
            )
        }
    }
}

// Linha tocável: ícone rosa num quadrado rosa claro de 44dp, título, descrição e seta
@Composable
private fun OpcaoEnvio(icone: ImageVector, titulo: String, descricao: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = descricao,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

// Alça dos sheets da aba Arquivos (igual à do NovoRegistroBottomSheet)
@Composable
internal fun AlcaBottomSheet() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .width(40.dp)
            .height(4.dp)
            .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
    )
}

// Faixa de 12sp com ícone (formatos aceitos, frame 35:408; aviso de consentimento, frame 35:630). O
// AlertBanner usa 14sp sem título, maior que o Figma destes sheets
@Composable
internal fun FaixaAviso(
    texto: String,
    icone: ImageVector,
    containerColor: Color,
    contentColor: Color,
    tamanhoIcone: Dp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = if (tamanhoIcone > 16.dp) 12.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(if (tamanhoIcone > 16.dp) 10.dp else 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = icone, contentDescription = null, tint = contentColor, modifier = Modifier.size(tamanhoIcone))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            fontWeight = fontWeight,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
    }
}
