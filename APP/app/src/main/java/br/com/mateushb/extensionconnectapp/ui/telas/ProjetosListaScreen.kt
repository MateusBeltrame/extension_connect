package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.FiltroProjetos
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.AppTopBar
import br.com.mateushb.extensionconnectapp.ui.components.AvatarStack
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SegmentedTabs
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.state.ListaProjetosUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val ABA_LISTA = 0
private const val ABA_QUADRO = 1

// Espaço no fim da rolagem para o último card não ficar sob o FAB
private val ESPACO_FAB = 88.dp

@Composable
fun ProjetosListaScreen(
    usuario: Usuario,
    listaUiState: ListaProjetosUiState,
    busca: String,
    filtroAtual: FiltroProjetos,
    onBuscaChange: (String) -> Unit,
    onAplicarFiltro: (FiltroProjetos) -> Unit,
    onAlternarSouOrientador: () -> Unit,
    onAlternarSoParados: () -> Unit,
    onLimparFiltros: () -> Unit,
    onTentarNovamente: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    // Sempre abre na aba Lista (spec 002, HU-01)
    var abaSelecionada by rememberSaveable { mutableIntStateOf(ABA_LISTA) }
    var filtrosAbertos by rememberSaveable { mutableStateOf(false) }

    // Detalhe (feature 4), criação (feature 3) e menu do card ainda não existem
    val mostrarEmBreve: () -> Unit = {
        escopo.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Disponível em breve")
        }
    }

    val alunoSemProjetos = usuario.perfil == PerfilUsuario.ALUNO &&
        listaUiState is ListaProjetosUiState.Sucesso && !listaUiState.existemProjetos

    Scaffold(
        topBar = {
            AppTopBar(title = "Projetos") {
                Box {
                    IconButton(onClick = { filtrosAbertos = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filtrar projetos",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    if (filtroAtual.ativo) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (listaUiState is ListaProjetosUiState.Sucesso && !alunoSemProjetos) {
                FloatingActionButton(
                    onClick = mostrarEmBreve,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Adicionar projeto")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SegmentedTabs(
                    items = listOf("Lista", "Quadro"),
                    selectedIndex = abaSelecionada,
                    onSelect = { abaSelecionada = it },
                    icons = listOf(Icons.AutoMirrored.Outlined.FormatListBulleted, Icons.Outlined.ViewColumn),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.height(44.dp)
                )
                AppTextField(
                    value = busca,
                    onValueChange = onBuscaChange,
                    placeholder = "Buscar por título, grupo ou participante",
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }

            when (listaUiState) {
                is ListaProjetosUiState.Carregando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ListaProjetosUiState.Erro -> {
                    ErroCarregamento(mensagem = listaUiState.mensagem, onTentarNovamente = onTentarNovamente)
                }

                is ListaProjetosUiState.Sucesso -> {
                    when {
                        !listaUiState.existemProjetos && usuario.perfil == PerfilUsuario.ALUNO ->
                            AlunoSemProjetos(onCriarProjetoClick = mostrarEmBreve)

                        !listaUiState.existemProjetos ->
                            MensagemCentral("Nenhum projeto cadastrado")

                        abaSelecionada == ABA_QUADRO ->
                            ProjetosQuadro(
                                usuario = usuario,
                                projetos = listaUiState.projetos,
                                hoje = listaUiState.hoje,
                                filtroAtual = filtroAtual,
                                onAlternarSouOrientador = onAlternarSouOrientador,
                                onAlternarSoParados = onAlternarSoParados,
                                onProjetoClick = mostrarEmBreve,
                                onMaisClick = mostrarEmBreve,
                                onLimparFiltros = onLimparFiltros
                            )

                        listaUiState.projetos.isEmpty() ->
                            NenhumProjetoEncontrado(onLimparFiltros = onLimparFiltros)

                        else ->
                            LazyColumn(
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = ESPACO_FAB),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(listaUiState.projetos, key = { it.projeto.id }) { resumo ->
                                    ProjetoCard(
                                        resumo = resumo,
                                        hoje = listaUiState.hoje,
                                        onClick = mostrarEmBreve,
                                        onMaisClick = mostrarEmBreve
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    if (filtrosAbertos && listaUiState is ListaProjetosUiState.Sucesso) {
        FiltrosProjetosBottomSheet(
            filtroAtual = filtroAtual,
            orientadores = listaUiState.orientadores,
            mostrarSouOrientador = usuario.perfil == PerfilUsuario.PROFESSOR,
            onAplicar = { filtro ->
                onAplicarFiltro(filtro)
                filtrosAbertos = false
            },
            onDispensar = { filtrosAbertos = false }
        )
    }
}

// ===== Estados de conteúdo =====

@Composable
private fun ErroCarregamento(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensagem,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onTentarNovamente) {
            Text("Tentar novamente")
        }
    }
}

@Composable
private fun MensagemCentral(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Frame 33:200
@Composable
private fun AlunoSemProjetos(onCriarProjetoClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = "Você ainda não participa de nenhum projeto",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Crie um projeto com seu grupo ou peça ao seu colega para adicionar você.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        PrimaryButton(
            text = "Criar projeto",
            onClick = onCriarProjetoClick,
            leadingIcon = Icons.Outlined.Add,
            fullWidth = false
        )
    }
}

@Composable
internal fun NenhumProjetoEncontrado(onLimparFiltros: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nenhum projeto encontrado",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = onLimparFiltros,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text("Limpar filtros", color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ===== Card de projeto (frame card-projeto) =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjetoCard(
    resumo: ProjetoResumo,
    hoje: LocalDate,
    onClick: () -> Unit,
    onMaisClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projeto = resumo.projeto
    AppCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "GRUPO ${projeto.nomeGrupo.uppercase()}",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onMaisClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Mais opções",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = projeto.titulo,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Orientação: ${resumo.orientador.nome}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(label = projeto.estagio.titulo, tone = projeto.estagio.tom())
                if (resumo.atrasado(hoje)) {
                    ChipComIcone("Atrasado", StatusTone.Red, Icons.Outlined.Schedule)
                }
                if (resumo.parado(hoje)) {
                    ChipComIcone("Parado", StatusTone.Neutral, Icons.Outlined.Pause)
                }
                if (projeto.aguardandoCienciaOrientador) {
                    ChipComIcone("Aguardando ciência", StatusTone.Purple, Icons.Outlined.Visibility)
                }
            }
            if (resumo.semCronograma) {
                ChipComIcone("Sem cronograma", StatusTone.Neutral, Icons.Outlined.CalendarToday)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { resumo.percentualConcluido / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                    Text(
                        text = "${resumo.etapasConcluidas}/${resumo.etapas.size} · ${resumo.percentualConcluido}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(members = resumo.participantes.map { iniciais(it.nome) to StatusTone.Pink })
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    textoDiasNoEstagio(resumo.diasNoEstagio(hoje))?.let { texto ->
                        Text(
                            text = texto,
                            style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    periodoAplicacao(projeto)?.let { periodo ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = periodo,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipComIcone(label: String, tone: StatusTone, icone: ImageVector) {
    StatusChip(
        label = label,
        tone = tone,
        leadingIcon = {
            Icon(imageVector = icone, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(12.dp))
        }
    )
}

// Chip selecionável (filtros do bottom sheet e atalhos do quadro): rosa com check quando selecionado,
// contornado quando não (frames 33:279 e 33:605)
@Composable
internal fun ChipFiltro(
    label: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    icone: ImageVector? = null,
    tamanhoTexto: TextUnit = 12.sp
) {
    val iconeExibido = if (selecionado) Icons.Outlined.Check else icone
    // Sem a área mínima de toque de 48dp do M3: o chip ficaria desalinhado dos chips informativos (32dp)
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        FilterChip(
            selected = selecionado,
            onClick = onClick,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = tamanhoTexto, letterSpacing = 0.sp)
                )
            },
            leadingIcon = iconeExibido?.let { { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(12.dp)) } },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selecionado,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = Color.Transparent
            ),
            modifier = Modifier.height(32.dp)
        )
    }
}

// ===== Apresentação dos dados do projeto =====

internal fun EstagioProjeto.tom(): StatusTone = when (this) {
    EstagioProjeto.EM_ANALISE -> StatusTone.Amber
    EstagioProjeto.EM_APLICACAO -> StatusTone.Blue
    EstagioProjeto.FINALIZADO -> StatusTone.Green
}

private val FORMATO_DIA_MES: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun periodoAplicacao(projeto: Projeto): String? {
    val inicio = projeto.aplicacaoInicio ?: return null
    val fim = projeto.aplicacaoFim ?: return null
    return try {
        "Aplicação ${LocalDate.parse(inicio).format(FORMATO_DIA_MES)} – ${LocalDate.parse(fim).format(FORMATO_DIA_MES)}"
    } catch (_: Exception) {
        null
    }
}

private fun textoDiasNoEstagio(dias: Int?): String? = when (dias) {
    null -> null
    0 -> "Neste estágio desde hoje"
    1 -> "Há 1 dia neste estágio"
    else -> "Há $dias dias neste estágio"
}

// "Mateus Silva" → "MS"; nome de uma palavra → primeira letra
private fun iniciais(nome: String): String {
    val partes = nome.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.isEmpty() -> "?"
        partes.size == 1 -> partes.first().take(1).uppercase()
        else -> (partes.first().take(1) + partes.last().take(1)).uppercase()
    }
}

// ===== Previews =====

private fun resumoExemplo(id: Int, grupo: String, titulo: String, estagio: EstagioProjeto, concluidas: Int, total: Int) =
    ProjetoResumo(
        projeto = Projeto(
            id = id, titulo = titulo, nomeGrupo = grupo, estagio = estagio,
            aplicacaoInicio = "2026-11-10", aplicacaoFim = "2026-12-05"
        ),
        orientador = Usuario(id = 9, nome = "Ana Ribeiro", perfil = PerfilUsuario.PROFESSOR),
        participantes = listOf("Mateus Silva", "Julia Lima", "Carla Rocha", "Igor Nunes", "Helena Dias")
            .mapIndexed { i, nome -> Usuario(id = i, nome = nome) },
        etapas = List(total) { Etapa(id = it, concluida = it < concluidas) },
        dataUltimaMudancaEstagio = "2026-10-13T12:00:00Z"
    )

@Preview(showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun ProjetosListaScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        ProjetosListaScreen(
            usuario = Usuario(id = 1, nome = "Mateus Silva"),
            listaUiState = ListaProjetosUiState.Sucesso(
                projetos = listOf(
                    resumoExemplo(1, "Raízes", "Horta comunitária na Escola Municipal Vila Nova", EstagioProjeto.EM_APLICACAO, 3, 5),
                    resumoExemplo(2, "Letras Vivas", "Clube de leitura na biblioteca comunitária", EstagioProjeto.EM_ANALISE, 0, 0)
                ),
                existemProjetos = true,
                orientadores = emptyList(),
                hoje = LocalDate.parse("2026-10-25")
            ),
            busca = "",
            filtroAtual = FiltroProjetos(),
            onBuscaChange = {}, onAplicarFiltro = {}, onAlternarSouOrientador = {}, onAlternarSoParados = {},
            onLimparFiltros = {}, onTentarNovamente = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun ProjetosListaVazioPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        ProjetosListaScreen(
            usuario = Usuario(id = 1, nome = "Mateus Silva"),
            listaUiState = ListaProjetosUiState.Sucesso(emptyList(), false, emptyList(), LocalDate.parse("2026-10-25")),
            busca = "",
            filtroAtual = FiltroProjetos(),
            onBuscaChange = {}, onAplicarFiltro = {}, onAlternarSouOrientador = {}, onAlternarSoParados = {},
            onLimparFiltros = {}, onTentarNovamente = {}
        )
    }
}
