package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.RegistroDiario
import br.com.mateushb.extensionconnectapp.model.TipoRegistro
import br.com.mateushb.extensionconnectapp.ui.components.AlertBanner
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SecondaryButton
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import kotlinx.coroutines.launch

// Sheet "Novo registro" do diário (spec 005, HU-03, frame 35:100). O tipo não é escolhido: vem do
// papel de quem publica. Como o MudarEstagioBottomSheet, não fecha por gesto nem por toque fora enquanto
// grava, e o erro de gravação [erro] aparece aqui dentro (um Snackbar da tela ficaria atrás do sheet)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NovoRegistroBottomSheet(
    tipo: TipoRegistro,
    etapas: List<Etapa>,
    salvando: Boolean,
    erro: String?,
    onPublicar: (texto: String, etapaId: Int?) -> Unit,
    onDispensar: () -> Unit
) {
    val salvandoAtual by rememberUpdatedState(salvando)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !salvandoAtual }
    )
    val escopo = rememberCoroutineScope()

    // Saveable: o que foi digitado sobrevive à rotação e ao erro de gravação
    var texto by rememberSaveable { mutableStateOf("") }
    var etapaId by rememberSaveable { mutableStateOf<Int?>(null) }
    var tentouPublicar by rememberSaveable { mutableStateOf(false) }

    val erroTexto = RegistroDiario.erroTexto(texto).takeIf { tentouPublicar }
    // A etapa escolhida pode ter sumido do cronograma enquanto o sheet estava aberto
    val etapaEscolhida = etapas.firstOrNull { it.id == etapaId }

    ModalBottomSheet(
        onDismissRequest = { if (!salvandoAtual) onDispensar() },
        sheetState = sheetState,
        sheetGesturesEnabled = !salvando,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Novo registro",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            FaixaTipoRegistro(tipo)

            // O limite impede digitar além do máximo (mesmo padrão do 7a)
            AppTextField(
                value = texto,
                onValueChange = { texto = it.take(RegistroDiario.MAX_TEXTO) },
                placeholder = "Conte o que foi feito, com detalhes",
                label = "O que foi feito?",
                singleLine = false,
                height = 110.dp,
                supportingText = erroTexto ?: "Mínimo de ${RegistroDiario.MIN_TEXTO} caracteres",
                isError = erroTexto != null,
                enabled = !salvando
            )

            CampoEtapa(
                etapas = etapas,
                etapa = etapaEscolhida,
                enabled = !salvando,
                onEtapaChange = { etapaId = it?.id }
            )

            erro?.let { AlertBanner(message = it, icon = Icons.Outlined.ErrorOutline) }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Cancelar",
                    // Fecha com a animação do sheet antes de tirá-lo da composição
                    onClick = { escopo.launch { sheetState.hide() }.invokeOnCompletion { onDispensar() } },
                    enabled = !salvando,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Publicar",
                    onClick = {
                        tentouPublicar = true
                        if (RegistroDiario.erroTexto(texto) == null) onPublicar(texto, etapaEscolhida?.id)
                    },
                    loading = salvando,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// "ⓘ Será registrado como: [Atividade]" (faixa cinza do frame 35:100)
@Composable
private fun FaixaTipoRegistro(tipo: TipoRegistro) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Será registrado como:",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        StatusChip(
            label = tipo.titulo,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// "Etapa relacionada (opcional)": "Nenhuma" e as etapas na ordem do cronograma. Sem etapas, desabilitado
@Composable
private fun CampoEtapa(
    etapas: List<Etapa>,
    etapa: Etapa?,
    enabled: Boolean,
    onEtapaChange: (Etapa?) -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }
    val semEtapas = etapas.isEmpty()
    Box {
        AppTextField(
            value = etapa?.nome.orEmpty(),
            onValueChange = {},
            placeholder = if (semEtapas) "Projeto sem etapas" else "Selecione uma etapa",
            label = "Etapa relacionada (opcional)",
            leadingIcon = { IconeCampo(Icons.Outlined.Flag) },
            trailingIcon = { IconeCampo(Icons.Outlined.KeyboardArrowDown) },
            onClick = { menuAberto = true },
            enabled = enabled && !semEtapas
        )
        DropdownMenu(
            expanded = menuAberto,
            onDismissRequest = { menuAberto = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DropdownMenuItem(
                text = { Text(text = "Nenhuma", style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    onEtapaChange(null)
                    menuAberto = false
                }
            )
            etapas.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(text = opcao.nome, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onEtapaChange(opcao)
                        menuAberto = false
                    }
                )
            }
        }
    }
}
