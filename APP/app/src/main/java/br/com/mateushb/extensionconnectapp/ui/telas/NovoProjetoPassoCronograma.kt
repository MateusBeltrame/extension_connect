package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.mateushb.extensionconnectapp.model.ErrosPasso2
import br.com.mateushb.extensionconnectapp.model.RascunhoEtapa
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber
import java.time.LocalDate

private val ESPACO_ENTRE_ETAPAS = 12.dp

// Passo 2 — "Etapas do cronograma" (frame 37:109)
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PassoCronograma(
    rascunho: RascunhoProjeto,
    enabled: Boolean,
    erros: ErrosPasso2?,
    onRascunhoChange: ((RascunhoProjeto) -> RascunhoProjeto) -> Unit,
    onAdicionarEtapa: (String) -> Unit,
    onRemoverEtapa: (Long) -> Unit,
    onMoverEtapa: (de: Int, para: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = ESPACO_FIM_PASSO),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TituloPasso(
                titulo = "Etapas do cronograma",
                subtitulo = "Adicione ao menos uma etapa. Você pode reordenar arrastando."
            )
            erros?.semEtapas?.let {
                Text(text = it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SUGESTÕES",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RascunhoProjeto.SUGESTOES_ETAPA.forEach { sugestao ->
                    ChipSugestao(
                        texto = sugestao,
                        usada = rascunho.sugestaoUsada(sugestao),
                        enabled = enabled,
                        onClick = { onAdicionarEtapa(sugestao) }
                    )
                }
            }
        }

        ListaEtapasArrastavel(
            rascunho = rascunho,
            enabled = enabled,
            erros = erros,
            onRascunhoChange = onRascunhoChange,
            onRemoverEtapa = onRemoverEtapa,
            onMoverEtapa = onMoverEtapa
        )

        BotaoAdicionarEtapa(enabled = enabled, onClick = { onAdicionarEtapa("") })
    }
}

// Column (e não LazyColumn): o passo inteiro rola e a lista é curta. O card é arrastado pela alça;
// ao passar da metade do vizinho, troca de posição e o deslocamento é compensado
@Composable
private fun ListaEtapasArrastavel(
    rascunho: RascunhoProjeto,
    enabled: Boolean,
    erros: ErrosPasso2?,
    onRascunhoChange: ((RascunhoProjeto) -> RascunhoProjeto) -> Unit,
    onRemoverEtapa: (Long) -> Unit,
    onMoverEtapa: (de: Int, para: Int) -> Unit
) {
    var arrastandoChave by remember { mutableStateOf<Long?>(null) }
    var deslocamento by remember { mutableFloatStateOf(0f) }
    val alturas = remember { mutableStateMapOf<Long, Int>() }
    val etapasAtuais by rememberUpdatedState(rascunho.etapas)
    val moverAtual by rememberUpdatedState(onMoverEtapa)

    Column(verticalArrangement = Arrangement.spacedBy(ESPACO_ENTRE_ETAPAS)) {
        rascunho.etapas.forEachIndexed { indice, etapa ->
            key(etapa.chave) {
                val arrastando = arrastandoChave == etapa.chave
                CardEtapa(
                    numero = indice + 1,
                    etapa = etapa,
                    enabled = enabled,
                    erroNome = erros?.nomes?.get(etapa.chave),
                    erroData = erros?.datas?.get(etapa.chave),
                    foraDoPeriodo = rascunho.foraDoPeriodo(etapa),
                    periodo = rascunho.dataInicio?.let { inicio ->
                        rascunho.dataFimPrevisto?.let { fim -> "${inicio.formatarDiaMes()} a ${fim.formatarDiaMes()}" }
                    },
                    onNomeChange = { nome ->
                        onRascunhoChange { r ->
                            r.copy(etapas = r.etapas.map { if (it.chave == etapa.chave) it.copy(nome = nome) else it })
                        }
                    },
                    onDataChange = { data ->
                        onRascunhoChange { r ->
                            r.copy(etapas = r.etapas.map { if (it.chave == etapa.chave) it.copy(dataPrevista = data) else it })
                        }
                    },
                    onRemoverClick = { onRemoverEtapa(etapa.chave) },
                    // TalkBack não arrasta: mesmas trocas como ações de acessibilidade
                    acoesAcessibilidade = listOfNotNull(
                        CustomAccessibilityAction("Mover para cima") { onMoverEtapa(indice, indice - 1); true }
                            .takeIf { indice > 0 },
                        CustomAccessibilityAction("Mover para baixo") { onMoverEtapa(indice, indice + 1); true }
                            .takeIf { indice < rascunho.etapas.lastIndex }
                    ),
                    alcaModifier = if (!enabled) Modifier else Modifier.pointerInput(etapa.chave) {
                        val espaco = ESPACO_ENTRE_ETAPAS.toPx()
                        detectDragGestures(
                            onDragStart = {
                                arrastandoChave = etapa.chave
                                deslocamento = 0f
                            },
                            onDragEnd = {
                                arrastandoChave = null
                                deslocamento = 0f
                            },
                            onDragCancel = {
                                arrastandoChave = null
                                deslocamento = 0f
                            },
                            onDrag = { mudanca, arrasto ->
                                mudanca.consume()
                                deslocamento += arrasto.y
                                val lista = etapasAtuais
                                val atual = lista.indexOfFirst { it.chave == etapa.chave }
                                if (deslocamento > 0 && atual < lista.lastIndex) {
                                    val alturaVizinho = alturas[lista[atual + 1].chave] ?: 0
                                    if (deslocamento > alturaVizinho / 2f) {
                                        moverAtual(atual, atual + 1)
                                        deslocamento -= alturaVizinho + espaco
                                    }
                                } else if (deslocamento < 0 && atual > 0) {
                                    val alturaVizinho = alturas[lista[atual - 1].chave] ?: 0
                                    if (-deslocamento > alturaVizinho / 2f) {
                                        moverAtual(atual, atual - 1)
                                        deslocamento += alturaVizinho + espaco
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .onGloballyPositioned { alturas[etapa.chave] = it.size.height }
                        .zIndex(if (arrastando) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (arrastando) deslocamento else 0f
                            scaleX = if (arrastando) 1.02f else 1f
                            scaleY = if (arrastando) 1.02f else 1f
                        }
                )
            }
        }
    }
}

@Composable
private fun CardEtapa(
    numero: Int,
    etapa: RascunhoEtapa,
    enabled: Boolean,
    erroNome: String?,
    erroData: String?,
    foraDoPeriodo: Boolean,
    periodo: String?,
    onNomeChange: (String) -> Unit,
    onDataChange: (LocalDate) -> Unit,
    onRemoverClick: () -> Unit,
    acoesAcessibilidade: List<CustomAccessibilityAction>,
    alcaModifier: Modifier,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { customActions = acoesAcessibilidade },
        contentPadding = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = alcaModifier.size(width = 28.dp, height = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DragIndicator,
                    contentDescription = "Arrastar para reordenar",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Etapa $numero",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                AppTextField(
                    value = etapa.nome,
                    onValueChange = onNomeChange,
                    placeholder = "Nome da etapa",
                    supportingText = erroNome,
                    isError = erroNome != null,
                    enabled = enabled
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CampoData(
                        valor = etapa.dataPrevista,
                        onValorChange = onDataChange,
                        placeholder = "Data prevista",
                        erro = erroData,
                        isWarning = foraDoPeriodo,
                        enabled = enabled
                    )
                    if (foraDoPeriodo) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Data fora do período do projeto" + (periodo?.let { " ($it)" } ?: ""),
                                fontSize = 12.sp,
                                color = WarningAmber
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onRemoverClick, enabled = enabled) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Remover etapa $numero",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Sugestão livre: contornada em rosa com "+"; já usada: cinza com check e sem toque
@Composable
private fun ChipSugestao(texto: String, usada: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .then(
                if (usada) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant, forma)
                } else {
                    Modifier
                        .background(MaterialTheme.colorScheme.surface, forma)
                        .border(1.dp, MaterialTheme.colorScheme.primary, forma)
                        .clickable(enabled = enabled, onClick = onClick)
                }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val cor = if (usada) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
        Icon(
            imageVector = if (usada) Icons.Outlined.Check else Icons.Outlined.Add,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(12.dp)
        )
        Text(text = texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cor)
    }
}

// Botão de contorno tracejado rosa (frame 37:251)
@Composable
private fun BotaoAdicionarEtapa(enabled: Boolean, onClick: () -> Unit) {
    val cor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = cor,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                    cornerRadius = CornerRadius(10.dp.toPx())
                )
            }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = cor, modifier = Modifier.size(18.dp))
        Text(text = "Adicionar etapa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cor)
    }
}
