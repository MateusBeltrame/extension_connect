package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.FiltroProjetos
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.Usuario
import java.time.LocalDate

// Cards visíveis por coluna antes do "Ver mais" (spec 002, HU-04)
private const val CARDS_POR_COLUNA = 3

/**
 * Aba Quadro da tela Projetos (frame 33:279): contadores por estágio, chips de resumo e colunas
 * Em análise / Em aplicação / Finalizado com rolagem horizontal. Recebe os projetos já filtrados.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProjetosQuadro(
    usuario: Usuario,
    projetos: List<ProjetoResumo>,
    hoje: LocalDate,
    filtroAtual: FiltroProjetos,
    onAlternarSouOrientador: () -> Unit,
    onAlternarSoParados: () -> Unit,
    onProjetoClick: (projetoId: Int) -> Unit,
    onMaisClick: () -> Unit,
    onLimparFiltros: () -> Unit
) {
    val porEstagio = projetos.groupBy { it.projeto.estagio }
    val emAplicacaoNaSemana = projetos.count { it.emAplicacaoNaSemana(hoje) }
    val parados = projetos.count { it.parado(hoje) }
    val aguardandoCiencia = projetos.count { it.projeto.aguardandoCienciaOrientador }
    // Estado efêmero: colunas expandidas por "Ver mais"
    var colunasExpandidas by rememberSaveable { mutableStateOf(emptySet<EstagioProjeto>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EstagioProjeto.entries.forEach { estagio ->
                ContadorEstagio(
                    quantidade = porEstagio[estagio].orEmpty().size,
                    estagio = estagio,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        FlowRow(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (usuario.perfil == PerfilUsuario.PROFESSOR) {
                ChipFiltro(
                    label = "Sou orientador",
                    selecionado = filtroAtual.souOrientador,
                    onClick = onAlternarSouOrientador
                )
            }
            if (emAplicacaoNaSemana > 0) {
                ChipInformativo("$emAplicacaoNaSemana em aplicação esta semana")
            }
            // Continua visível com o filtro ligado, para poder desligá-lo
            if (parados > 0 || filtroAtual.soParados) {
                ChipFiltro(
                    label = if (parados == 1) "1 parado" else "$parados parados",
                    selecionado = filtroAtual.soParados,
                    onClick = onAlternarSoParados,
                    icone = Icons.Outlined.Pause
                )
            }
            if (aguardandoCiencia > 0) {
                ChipInformativo("$aguardandoCiencia aguardando ciência", Icons.Outlined.Visibility)
            }
        }

        if (projetos.isEmpty()) {
            NenhumProjetoEncontrado(onLimparFiltros = onLimparFiltros)
        } else {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(PaddingValues(horizontal = 20.dp)),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                EstagioProjeto.entries.forEach { estagio ->
                    val expandida = estagio in colunasExpandidas
                    ColunaEstagio(
                        estagio = estagio,
                        projetos = porEstagio[estagio].orEmpty(),
                        hoje = hoje,
                        expandida = expandida,
                        onAlternarExpandida = {
                            colunasExpandidas = if (expandida) colunasExpandidas - estagio else colunasExpandidas + estagio
                        },
                        onProjetoClick = onProjetoClick,
                        onMaisClick = onMaisClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ContadorEstagio(quantidade: Int, estagio: EstagioProjeto, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = quantidade.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = estagio.tom().onContainer
            )
            Text(
                text = estagio.titulo,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// Mesmo visual do chip de filtro desmarcado, mas só informativo (sem toque)
@Composable
private fun ChipInformativo(label: String, icone: ImageVector? = null) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icone?.let {
            Icon(imageVector = it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, letterSpacing = 0.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColunaEstagio(
    estagio: EstagioProjeto,
    projetos: List<ProjetoResumo>,
    hoje: LocalDate,
    expandida: Boolean,
    onAlternarExpandida: () -> Unit,
    onProjetoClick: (projetoId: Int) -> Unit,
    onMaisClick: () -> Unit
) {
    val tom = estagio.tom()
    Column(
        modifier = Modifier
            .width(318.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tom.onContainer)
            )
            Text(
                text = estagio.titulo,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = tom.onContainer
            )
            Text(
                text = projetos.size.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (projetos.isEmpty()) {
            Text(
                text = "Nenhum projeto",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            val visiveis = if (expandida) projetos else projetos.take(CARDS_POR_COLUNA)
            visiveis.forEach { resumo ->
                ProjetoCard(resumo = resumo, hoje = hoje, onClick = { onProjetoClick(resumo.projeto.id) }, onMaisClick = onMaisClick)
            }
            if (projetos.size > CARDS_POR_COLUNA) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAlternarExpandida)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (expandida) "Ver menos" else "Ver mais",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
