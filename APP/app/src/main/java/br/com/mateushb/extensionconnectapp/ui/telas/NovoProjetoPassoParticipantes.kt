package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.CandidatoParticipante
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.Avatar
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber
import br.com.mateushb.extensionconnectapp.ui.theme.WarningContainerLight
import br.com.mateushb.extensionconnectapp.ui.theme.WarningOutlineLight

// Passo 3 — participantes (frames 37:267 e 37:393 no limite de 10)
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PassoParticipantes(
    rascunho: RascunhoProjeto,
    candidatos: List<CandidatoParticipante>,
    erro: String?,
    enabled: Boolean,
    onAdicionarParticipante: (Usuario) -> Unit,
    onRemoverParticipante: (Int) -> Unit
) {
    var busca by rememberSaveable { mutableStateOf("") }
    val termo = ProjetoResumo.normalizar(busca)
    val buscaValida = termo.length >= RascunhoProjeto.MIN_LETRAS_BUSCA && !rascunho.limiteAtingido
    val escolhidos = rascunho.participantes.map { it.id }.toSet()
    val resultados = if (buscaValida) {
        candidatos.filter { it.aluno.id !in escolhidos && it.aluno.correspondeABusca(termo) }
    } else {
        emptyList()
    }
    // Escolhido que entrou em outro projeto ou foi desativado durante o wizard (o Flow de candidatos é
    // reativo; desativado some da lista de candidatos)
    val candidatoPorId = candidatos.associateBy { it.aluno.id }
    val indisponiveis = escolhidos.filter { id -> candidatoPorId[id]?.bloqueado != false }.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = ESPACO_FIM_PASSO),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TituloPasso(titulo = "Participantes", subtitulo = "Busque alunos cadastrados por nome ou e-mail.")

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ContadorParticipantes(quantidade = rascunho.participantes.size, limiteAtingido = rascunho.limiteAtingido)
            erro?.let {
                Text(text = it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
            }
        }

        if (rascunho.participantes.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rascunho.participantes.forEach { aluno ->
                    ChipParticipante(
                        aluno = aluno,
                        ocupado = aluno.id in indisponiveis,
                        enabled = enabled,
                        onRemoverClick = { onRemoverParticipante(aluno.id) }
                    )
                }
            }
        }

        if (rascunho.limiteAtingido) {
            AppTextField(
                value = "",
                onValueChange = {},
                placeholder = "Busca bloqueada: limite de ${RascunhoProjeto.LIMITE_PARTICIPANTES} participantes",
                leadingIcon = { IconeCampo(Icons.Outlined.Lock) },
                enabled = false
            )
        } else {
            AppTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = "Buscar aluno",
                leadingIcon = { IconeCampo(Icons.Outlined.Search) },
                supportingText = when {
                    !buscaValida -> "Digite ao menos 3 letras"
                    resultados.isEmpty() -> "Nenhum aluno encontrado"
                    else -> null
                },
                enabled = enabled
            )
            if (resultados.isNotEmpty()) {
                AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        resultados.forEachIndexed { indice, candidato ->
                            if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ResultadoAluno(
                                candidato = candidato,
                                enabled = enabled,
                                onAdicionarClick = { onAdicionarParticipante(candidato.aluno) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Cartão "N de 10 participantes"; no limite fica amarelo com "Limite atingido" (frame 37:393)
@Composable
private fun ContadorParticipantes(quantidade: Int, limiteAtingido: Boolean) {
    val forma = RoundedCornerShape(10.dp)
    val corTexto = if (limiteAtingido) WarningAmber else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (limiteAtingido) WarningContainerLight else MaterialTheme.colorScheme.surface, forma)
            .border(1.dp, if (limiteAtingido) WarningOutlineLight else MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Group,
            contentDescription = null,
            tint = if (limiteAtingido) WarningAmber else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "$quantidade de ${RascunhoProjeto.LIMITE_PARTICIPANTES} participantes",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = corTexto,
            modifier = Modifier.weight(1f)
        )
        if (limiteAtingido) {
            Text(text = "Limite atingido", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WarningAmber)
        }
    }
}

@Composable
private fun ChipParticipante(aluno: Usuario, ocupado: Boolean, enabled: Boolean, onRemoverClick: () -> Unit) {
    val forma = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, forma)
            .border(1.dp, if (ocupado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Avatar(initials = iniciais(aluno.nome), tone = if (ocupado) StatusTone.Red else StatusTone.Pink, size = 24.dp)
        Text(text = aluno.nome, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = onRemoverClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Remover ${aluno.nome}",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// Aluno já em projeto não finalizado: esmaecido, sem "+", com "Já participa de Grupo X"
@Composable
private fun ResultadoAluno(candidato: CandidatoParticipante, enabled: Boolean, onAdicionarClick: () -> Unit) {
    val aluno = candidato.aluno
    val tom = if (candidato.bloqueado) StatusTone.Neutral else StatusTone.Pink
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (candidato.bloqueado) 0.55f else 1f)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(initials = iniciais(aluno.nome), tone = tom, size = 36.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = aluno.nome, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = listOfNotNull(aluno.email, aluno.curso?.titulo).joinToString(" · "),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
            candidato.grupoAtivo?.let { grupo ->
                StatusChip(
                    label = "Já participa de Grupo $grupo",
                    tone = StatusTone.Neutral,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = StatusTone.Neutral.onContainer,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        IconButton(
            onClick = onAdicionarClick,
            enabled = enabled && !candidato.bloqueado,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(tom.container, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Adicionar ${aluno.nome}",
                    tint = if (candidato.bloqueado) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
