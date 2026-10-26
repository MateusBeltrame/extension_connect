package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.FiltroDiario
import br.com.mateushb.extensionconnectapp.model.RegistroDiarioDetalhes
import br.com.mateushb.extensionconnectapp.model.TipoRegistro
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.Avatar
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.state.ListaRegistrosDiarioUiState
import java.time.LocalDateTime

// Aba Diário dos detalhes do projeto (spec 005, frame 35:2): chips de filtro e um card por registro,
// do mais recente para o mais antigo. Entra como itens da LazyColumn da tela, para cada card ter a
// própria chave e a lista rolar inteira
internal fun LazyListScope.abaDiario(
    listaUiState: ListaRegistrosDiarioUiState,
    filtro: FiltroDiario,
    podePublicar: Boolean,
    agora: LocalDateTime,
    podeExcluir: (RegistroDiarioDetalhes) -> Boolean,
    onFiltroChange: (FiltroDiario) -> Unit,
    onExcluirClick: (RegistroDiarioDetalhes) -> Unit,
    onTentarNovamente: () -> Unit
) {
    item(key = "diario-filtros") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroDiario.entries.forEach { opcao ->
                ChipFiltro(
                    label = opcao.titulo,
                    selecionado = opcao == filtro,
                    onClick = { onFiltroChange(opcao) },
                    tamanhoTexto = 13.sp,
                    mostrarCheck = false
                )
            }
        }
    }

    when (listaUiState) {
        is ListaRegistrosDiarioUiState.Carregando -> item(key = "diario-carregando") {
            Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ListaRegistrosDiarioUiState.Erro -> item(key = "diario-erro") {
            Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                ErrorRetry(message = listaUiState.mensagem, onRetry = onTentarNovamente)
            }
        }

        is ListaRegistrosDiarioUiState.Sucesso -> {
            val registros = filtro.aplicar(listaUiState.registros)
            if (registros.isEmpty()) {
                item(key = "diario-vazio") {
                    DiarioVazio(filtro = filtro, podePublicar = podePublicar)
                }
            } else {
                items(registros, key = { "registro-${it.registro.id}" }) { detalhes ->
                    CardRegistro(
                        detalhes = detalhes,
                        agora = agora,
                        podeExcluir = podeExcluir(detalhes),
                        onExcluirClick = { onExcluirClick(detalhes) }
                    )
                }
            }
        }
    }
}

// ===== Card de registro =====

// Atividade: card branco com os chips "Atividade" e "Etapa". Orientação: card rosa com borda, o rótulo
// "Orientação do professor" no topo, avatar e chip de etapa brancos e sem o chip de tipo
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardRegistro(
    detalhes: RegistroDiarioDetalhes,
    agora: LocalDateTime,
    podeExcluir: Boolean,
    onExcluirClick: () -> Unit
) {
    val orientacao = detalhes.registro.tipo == TipoRegistro.ORIENTACAO
    val nomeAutor = detalhes.autor?.nome.orEmpty()

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (orientacao) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified,
        // Rosa da marca a 22% sobre o fundo = #F9C6E1 do Figma, e acompanha o tema
        border = if (orientacao) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)) else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (orientacao) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Orientação do professor",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, letterSpacing = 0.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Avatar(
                    initials = iniciais(nomeAutor),
                    tone = StatusTone.Pink,
                    size = 32.dp,
                    containerColor = if (orientacao) MaterialTheme.colorScheme.surface else StatusTone.Pink.container
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = nomeAutor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = detalhes.dataExibida(agora),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (podeExcluir) {
                    MenuRegistro(onExcluirClick = onExcluirClick)
                }
            }

            val etapa = detalhes.etapa
            if (!orientacao || etapa != null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!orientacao) StatusChip(label = TipoRegistro.ATIVIDADE.titulo)
                    if (etapa != null) {
                        // Sobre o card de orientação, fundo e texto do tema (surface/onSurfaceVariant)
                        val corTexto = if (orientacao) MaterialTheme.colorScheme.onSurfaceVariant else StatusTone.Neutral.onContainer
                        StatusChip(
                            label = "Etapa: ${etapa.nome}",
                            containerColor = if (orientacao) MaterialTheme.colorScheme.surface else StatusTone.Neutral.container,
                            contentColor = corTexto,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Flag,
                                    contentDescription = null,
                                    tint = corTexto,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                    }
                }
            }

            Text(
                text = detalhes.registro.texto,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ⋮ do próprio registro (18dp no Figma); a área de toque continua com 32dp
@Composable
private fun MenuRegistro(onExcluirClick: () -> Unit) {
    var menuAberto by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menuAberto = true }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Opções do registro",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = menuAberto,
            onDismissRequest = { menuAberto = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DropdownMenuItem(
                text = { Text("Excluir registro", style = MaterialTheme.typography.bodyLarge) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    menuAberto = false
                    onExcluirClick()
                }
            )
        }
    }
}

// ===== Vazio e exclusão =====

@Composable
private fun DiarioVazio(filtro: FiltroDiario, podePublicar: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = filtro.mensagemVazio,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (filtro == FiltroDiario.TODOS && podePublicar) {
            Text(
                text = "Toque em Novo registro para contar o que o grupo fez.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun ConfirmacaoExclusaoRegistroDialog(onConfirmar: () -> Unit, onDispensar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDispensar,
        title = { Text("Confirmar exclusão") },
        text = { Text("Deseja realmente excluir este registro do diário?") },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDispensar) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
