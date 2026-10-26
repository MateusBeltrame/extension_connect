package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes
import br.com.mateushb.extensionconnectapp.model.RascunhoAplicacao
import br.com.mateushb.extensionconnectapp.model.TransicaoEstagio
import br.com.mateushb.extensionconnectapp.ui.components.AlertBanner
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SecondaryButton
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber
import br.com.mateushb.extensionconnectapp.ui.theme.WarningContainerLight
import kotlinx.coroutines.launch
import java.time.LocalDate

// Sheet "Mudar estágio" (spec 004, HU-07/HU-08): 7a mover para Em aplicação (33:804), 7b finalizar
// (33:988) e 7c voltar estágio (33:1171). Não fecha por gesto nem por toque fora enquanto grava; o erro
// de gravação [erro] aparece aqui dentro, porque um Snackbar da tela ficaria atrás do sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MudarEstagioBottomSheet(
    transicao: TransicaoEstagio,
    detalhes: ProjetoDetalhes,
    salvando: Boolean,
    erro: String?,
    onConfirmar: (aplicacao: RascunhoAplicacao?, justificativa: String?) -> Unit,
    onDispensar: () -> Unit
) {
    val salvandoAtual by rememberUpdatedState(salvando)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !salvandoAtual }
    )
    val escopo = rememberCoroutineScope()
    val projeto = detalhes.projeto

    // Saveable e por transição: o que foi digitado sobrevive à rotação e ao erro de gravação
    val inicial = RascunhoAplicacao.de(projeto)
    var inicio by rememberSaveable(transicao) { mutableStateOf<LocalDate?>(inicial.inicio) }
    var fim by rememberSaveable(transicao) { mutableStateOf<LocalDate?>(inicial.fim) }
    var descricao by rememberSaveable(transicao) { mutableStateOf(inicial.descricao) }
    var justificativa by rememberSaveable(transicao) { mutableStateOf("") }
    var tentouConfirmar by rememberSaveable(transicao) { mutableStateOf(false) }

    val aplicacao = RascunhoAplicacao(inicio = inicio, fim = fim, descricao = descricao)
    val errosAplicacao = aplicacao.erros(projeto).takeIf { tentouConfirmar }
    val erroJustificativa = TransicaoEstagio.erroJustificativa(justificativa).takeIf { tentouConfirmar }

    val confirmar = {
        tentouConfirmar = true
        when {
            transicao == TransicaoEstagio.AVANCAR_PARA_APLICACAO -> {
                if (aplicacao.erros(projeto).valido) onConfirmar(aplicacao, null)
            }
            transicao.voltar -> {
                if (TransicaoEstagio.erroJustificativa(justificativa) == null) onConfirmar(null, justificativa)
            }
            else -> onConfirmar(null, null)
        }
    }

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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(label = transicao.origem.titulo, tone = transicao.origem.tom())
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "para",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                StatusChip(label = transicao.destino.titulo, tone = transicao.destino.tom())
            }
            Text(
                text = when (transicao) {
                    TransicaoEstagio.AVANCAR_PARA_APLICACAO -> "Mover para Em aplicação"
                    TransicaoEstagio.FINALIZAR -> "Finalizar projeto"
                    else -> "Voltar para ${transicao.destino.titulo}"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            when (transicao) {
                TransicaoEstagio.AVANCAR_PARA_APLICACAO -> {
                    TextoApoio("Informe o período em que o projeto será aplicado.")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CampoData(
                            valor = inicio,
                            onValorChange = { inicio = it },
                            label = "Início da aplicação",
                            erro = errosAplicacao?.inicio,
                            enabled = !salvando,
                            modifier = Modifier.weight(1f)
                        )
                        CampoData(
                            valor = fim,
                            onValorChange = { fim = it },
                            label = "Fim da aplicação",
                            erro = errosAplicacao?.fim,
                            enabled = !salvando,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // O limite impede digitar além do máximo (mesmo padrão do wizard)
                    AppTextField(
                        value = descricao,
                        onValueChange = { descricao = it.take(RascunhoAplicacao.MAX_DESCRICAO) },
                        placeholder = "Ex.: oficinas semanais com as turmas",
                        label = "Descrição curta da aplicação",
                        singleLine = false,
                        height = 84.dp,
                        counter = "${descricao.length}/${RascunhoAplicacao.MAX_DESCRICAO}",
                        supportingText = errosAplicacao?.descricao,
                        isError = errosAplicacao?.descricao != null,
                        enabled = !salvando
                    )
                }

                TransicaoEstagio.FINALIZAR -> {
                    val pendentes = detalhes.etapasPendentes
                    if (pendentes.isNotEmpty()) {
                        AlertBanner(
                            message = textoPendentes(pendentes),
                            icon = Icons.Outlined.WarningAmber,
                            containerColor = WarningContainerLight,
                            contentColor = WarningAmber,
                            iconSize = 18.dp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        TextoApoio(
                            "Ao finalizar, o projeto passa a aparecer no Mural público, com as fotos autorizadas.",
                            Modifier.weight(1f)
                        )
                    }
                }

                TransicaoEstagio.VOLTAR_PARA_ANALISE, TransicaoEstagio.VOLTAR_PARA_APLICACAO -> {
                    TextoApoio("Explique o motivo. A justificativa fica registrada no histórico do projeto.")
                    AppTextField(
                        value = justificativa,
                        onValueChange = { justificativa = it },
                        placeholder = "Ex.: precisamos refazer o planejamento",
                        label = "Justificativa",
                        singleLine = false,
                        height = 96.dp,
                        supportingText = erroJustificativa ?: "Mínimo de ${TransicaoEstagio.MIN_JUSTIFICATIVA} caracteres",
                        isError = erroJustificativa != null,
                        enabled = !salvando
                    )
                }
            }

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
                    text = when (transicao) {
                        TransicaoEstagio.AVANCAR_PARA_APLICACAO -> "Confirmar"
                        TransicaoEstagio.FINALIZAR -> "Finalizar"
                        else -> "Voltar estágio"
                    },
                    onClick = confirmar,
                    loading = salvando,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TextoApoio(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// "Ainda há 2 etapas pendentes: Relatório final e Apresentação. Você pode finalizar mesmo assim."
private fun textoPendentes(pendentes: List<Etapa>): String {
    val quantidade = if (pendentes.size == 1) "1 etapa pendente" else "${pendentes.size} etapas pendentes"
    return "Ainda há $quantidade: ${juntarNomes(pendentes.map { it.nome })}. Você pode finalizar mesmo assim."
}

// Vírgulas entre os nomes e "e" antes do último
private fun juntarNomes(nomes: List<String>): String = when (nomes.size) {
    0 -> ""
    1 -> nomes.first()
    else -> nomes.dropLast(1).joinToString(", ") + " e " + nomes.last()
}
