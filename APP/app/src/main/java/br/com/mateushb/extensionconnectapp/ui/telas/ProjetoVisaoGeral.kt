package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.EventoHistorico
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.model.dataLocalDoInstante
import br.com.mateushb.extensionconnectapp.model.lerDataIso
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.Avatar
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.theme.ErrorRed
import java.time.LocalDate

// Cards da aba Visão geral dos detalhes do projeto (spec 004, frames 34:2 e 34:220)

private const val LINHAS_DESCRICAO = 4

// ===== Descrição =====

@Composable
internal fun CardDescricao(projeto: Projeto) {
    var expandida by rememberSaveable { mutableStateOf(false) }
    var excedeLimite by rememberSaveable { mutableStateOf(false) }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TituloCard("Descrição")
            Text(
                text = projeto.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expandida) Int.MAX_VALUE else LINHAS_DESCRICAO,
                // Só mostra "Ver mais" se o texto passar de 4 linhas
                onTextLayout = { if (!expandida) excedeLimite = it.hasVisualOverflow }
            )
            if (excedeLimite || expandida) {
                Text(
                    text = if (expandida) "Ver menos" else "Ver mais",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expandida = !expandida }
                )
            }
            StatusChip(
                label = projeto.tema.titulo,
                tone = StatusTone.Neutral,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Sell,
                        contentDescription = null,
                        tint = StatusTone.Neutral.onContainer,
                        modifier = Modifier.size(12.dp)
                    )
                }
            )
        }
    }
}

// ===== Participantes =====

// Orientador primeiro (avatar roxo + chip "Orientação"), depois os alunos por nome; o "N de 10"
// conta só os alunos
@Composable
internal fun CardParticipantes(resumo: ProjetoResumo) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TituloCard("Participantes", Modifier.weight(1f))
                StatusChip(label = "${resumo.participantes.size} de ${RascunhoProjeto.LIMITE_PARTICIPANTES}")
            }
            LinhaPessoa(resumo.orientador, StatusTone.Purple, papel = "Orientação")
            resumo.participantes.forEach { aluno ->
                LinhaPessoa(aluno, StatusTone.Pink)
            }
        }
    }
}

@Composable
private fun LinhaPessoa(pessoa: Usuario, tom: StatusTone, papel: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Avatar(initials = iniciais(pessoa.nome), tone = tom, size = 36.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = pessoa.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = pessoa.perfil.titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
        papel?.let { StatusChip(label = it, tone = tom) }
    }
}

// ===== Cronograma =====

@Composable
internal fun CardCronograma(
    etapas: List<Etapa>,
    hoje: LocalDate,
    somenteLeitura: Boolean,
    podeMarcar: Boolean,
    onAlternarEtapa: (Etapa) -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TituloCard("Cronograma", Modifier.weight(1f))
                if (etapas.isNotEmpty()) {
                    Text(
                        text = "${etapas.count { it.concluida }} de ${etapas.size} concluídas",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (etapas.isEmpty()) {
                Text(
                    text = "Nenhuma etapa cadastrada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            etapas.forEach { etapa ->
                LinhaEtapa(
                    etapa = etapa,
                    // Projeto finalizado não tem etapa "atrasada" (HU-06)
                    atrasada = !somenteLeitura && etapa.atrasada(hoje),
                    somenteLeitura = somenteLeitura,
                    podeMarcar = podeMarcar,
                    onAlternar = { onAlternarEtapa(etapa) }
                )
            }
        }
    }
}

@Composable
private fun LinhaEtapa(
    etapa: Etapa,
    atrasada: Boolean,
    somenteLeitura: Boolean,
    podeMarcar: Boolean,
    onAlternar: () -> Unit
) {
    val esmaecida = etapa.concluida || somenteLeitura
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = etapa.concluida, enabled = podeMarcar, role = Role.Checkbox, onValueChange = { onAlternar() })
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CaixaEtapa(concluida = etapa.concluida, desabilitada = somenteLeitura)
        Text(
            text = etapa.nome,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (esmaecida) FontWeight.Medium else FontWeight.SemiBold,
            color = if (esmaecida) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (atrasada) {
            ChipComIcone("Atrasada", StatusTone.Red, Icons.Outlined.Schedule)
        }
        Text(
            text = dataOuVazio(etapa.dataPrevista) { it.formatarDiaMes() },
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, letterSpacing = 0.sp),
            color = if (atrasada) ErrorRed else MaterialTheme.colorScheme.outline
        )
    }
}

// Checkbox de 20dp com cantos de 5dp (frame checkbox): o Checkbox do M3 não tem esse formato nem os
// estados desabilitados do Figma (34:220: fundo cinza com check branco, ou vazio com borda clara)
@Composable
private fun CaixaEtapa(concluida: Boolean, desabilitada: Boolean) {
    val formato = RoundedCornerShape(5.dp)
    val modificador = when {
        concluida -> Modifier.background(
            if (desabilitada) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
            formato
        )
        desabilitada -> Modifier
            .background(MaterialTheme.colorScheme.background, formato)
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, formato)
        else -> Modifier
            .background(MaterialTheme.colorScheme.surface, formato)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, formato)
    }
    Box(Modifier.size(20.dp).then(modificador), contentAlignment = Alignment.Center) {
        if (concluida) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

// ===== Histórico de estágios =====

@Composable
internal fun CardHistorico(historico: List<EventoHistorico>) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TituloCard("Histórico de estágios")
            historico.forEachIndexed { indice, evento ->
                LinhaHistorico(evento = evento, maisRecente = indice == 0)
            }
        }
    }
}

@Composable
private fun LinhaHistorico(evento: EventoHistorico, maisRecente: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        // Marcador: rosa no evento mais recente, cinza nos demais (alinhado aos chips)
        Box(Modifier.padding(top = 6.dp).width(12.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (maisRecente) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                evento.estagioAnterior?.let { anterior ->
                    StatusChip(label = anterior.titulo, tone = anterior.tom())
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "para",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                }
                StatusChip(label = evento.historico.estagio.titulo, tone = evento.historico.estagio.tom())
            }
            Text(
                text = listOfNotNull(evento.autor?.nome, dataDoInstante(evento.historico.data)).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline
            )
            evento.historico.justificativa?.takeIf { it.isNotBlank() }?.let { justificativa ->
                Text(
                    text = justificativa,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
            if (evento.criacao) {
                Text(
                    text = "Projeto criado",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===== Aba Arquivos (feature 6) =====

// ===== Apoio =====

@Composable
internal fun TituloCard(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

private fun dataOuVazio(iso: String, formatar: (LocalDate) -> String): String =
    lerDataIso(iso)?.let(formatar).orEmpty()

// Histórico grava Instant (UTC); exibe a data no fuso do aparelho
private fun dataDoInstante(iso: String): String? = dataLocalDoInstante(iso)?.formatarData()
