package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.FiltroProjetos
import br.com.mateushb.extensionconnectapp.model.TemaProjeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import kotlinx.coroutines.launch

/**
 * Bottom sheet "Filtros" da tela Projetos (frame 33:605). Edita um rascunho local: só vale ao tocar em
 * Aplicar; fechar arrastando, tocando fora ou com o voltar descarta as mudanças (spec 002, HU-05).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun FiltrosProjetosBottomSheet(
    filtroAtual: FiltroProjetos,
    orientadores: List<Usuario>,
    mostrarSouOrientador: Boolean,
    onAplicar: (FiltroProjetos) -> Unit,
    onDispensar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val escopo = rememberCoroutineScope()
    // Saveable: as escolhas ainda não aplicadas sobrevivem à rotação (o sheet também reabre)
    var rascunho by rememberSaveable(stateSaver = SAVER_FILTRO) { mutableStateOf(filtroAtual) }
    var temaExpandido by remember { mutableStateOf(false) }
    var orientadorExpandido by remember { mutableStateOf(false) }
    val nomeOrientador = orientadores.firstOrNull { it.id == rascunho.orientadorId }?.nome

    ModalBottomSheet(
        onDismissRequest = onDispensar,
        sheetState = sheetState,
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
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Limpar",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp, letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { rascunho = FiltroProjetos() }
                        .padding(4.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Estágio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                // Escolha única: tocar no selecionado volta para "todos"
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstagioProjeto.entries.forEach { estagio ->
                        val selecionado = rascunho.estagio == estagio
                        ChipFiltro(
                            label = estagio.titulo,
                            selecionado = selecionado,
                            onClick = { rascunho = rascunho.copy(estagio = if (selecionado) null else estagio) },
                            tamanhoTexto = 13.sp
                        )
                    }
                }
            }

            Box {
                AppTextField(
                    value = rascunho.tema?.titulo ?: "Todos os temas",
                    onValueChange = {},
                    placeholder = "Todos os temas",
                    label = "Tema",
                    trailingIcon = { IconeSeta() },
                    onClick = { temaExpandido = true }
                )
                DropdownMenu(
                    expanded = temaExpandido,
                    onDismissRequest = { temaExpandido = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos os temas") },
                        onClick = {
                            rascunho = rascunho.copy(tema = null)
                            temaExpandido = false
                        }
                    )
                    TemaProjeto.entries.forEach { tema ->
                        DropdownMenuItem(
                            text = { Text(tema.titulo) },
                            onClick = {
                                rascunho = rascunho.copy(tema = tema)
                                temaExpandido = false
                            }
                        )
                    }
                }
            }

            Box {
                AppTextField(
                    value = nomeOrientador ?: "Todos os orientadores",
                    onValueChange = {},
                    placeholder = "Todos os orientadores",
                    label = "Orientador",
                    trailingIcon = { IconeSeta() },
                    onClick = { orientadorExpandido = true }
                )
                DropdownMenu(
                    expanded = orientadorExpandido,
                    onDismissRequest = { orientadorExpandido = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos os orientadores") },
                        onClick = {
                            rascunho = rascunho.copy(orientadorId = null)
                            orientadorExpandido = false
                        }
                    )
                    orientadores.forEach { orientador ->
                        DropdownMenuItem(
                            text = { Text(orientador.nome) },
                            onClick = {
                                rascunho = rascunho.copy(orientadorId = orientador.id)
                                orientadorExpandido = false
                            }
                        )
                    }
                }
            }

            if (mostrarSouOrientador) {
                LinhaSwitch(
                    titulo = "Sou orientador",
                    descricao = "Mostra só os projetos que você orienta",
                    marcado = rascunho.souOrientador,
                    onMarcadoChange = { rascunho = rascunho.copy(souOrientador = it) }
                )
            }
            LinhaSwitch(
                titulo = "Só parados",
                descricao = "Sem atualização há mais de 30 dias",
                marcado = rascunho.soParados,
                onMarcadoChange = { rascunho = rascunho.copy(soParados = it) }
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { rascunho = FiltroProjetos() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "Limpar",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PrimaryButton(
                    text = "Aplicar",
                    onClick = {
                        // Anima o fechamento antes de entregar o filtro (que remove o sheet da composição)
                        escopo.launch { sheetState.hide() }.invokeOnCompletion { onAplicar(rascunho) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// FiltroProjetos não é Parcelable: salvo como lista de valores simples
private val SAVER_FILTRO = Saver<FiltroProjetos, List<Any?>>(
    save = { listOf(it.estagio?.name, it.tema?.name, it.orientadorId, it.souOrientador, it.soParados) },
    restore = { valores ->
        FiltroProjetos(
            estagio = EstagioProjeto.entries.firstOrNull { it.name == valores[0] },
            tema = TemaProjeto.entries.firstOrNull { it.name == valores[1] },
            orientadorId = valores[2] as Int?,
            souOrientador = valores[3] as Boolean,
            soParados = valores[4] as Boolean
        )
    }
)

@Composable
private fun IconeSeta() {
    Icon(imageVector = Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
}

@Composable
private fun LinhaSwitch(
    titulo: String,
    descricao: String,
    marcado: Boolean,
    onMarcadoChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp, letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = descricao,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
        Switch(
            checked = marcado,
            onCheckedChange = onMarcadoChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}
