package br.com.mateushb.extensionconnectapp.ui.telas

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.EventoHistorico
import br.com.mateushb.extensionconnectapp.model.HistoricoEstagio
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.RascunhoAplicacao
import br.com.mateushb.extensionconnectapp.model.TemaProjeto
import br.com.mateushb.extensionconnectapp.model.TransicaoEstagio
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.model.lerDataIso
import br.com.mateushb.extensionconnectapp.ui.components.AlertBanner
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTopBar
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SecondaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SegmentedTabs
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.state.AcaoProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.DetalhesProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.MudarEstagioUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import br.com.mateushb.extensionconnectapp.ui.theme.InfoContainerLight
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

private const val ABA_VISAO_GERAL = 0

// Detalhes do projeto (spec 004): card de resumo com as ações de estágio, abas e aba Visão geral.
// Frames 34:2 (orientador) e 34:220 (finalizado, somente leitura)
@Composable
fun ProjetoDetalhesScreen(
    usuario: Usuario,
    detalhesUiState: DetalhesProjetoUiState,
    mudarEstagioUiState: MudarEstagioUiState,
    acaoUiState: AcaoProjetoUiState,
    onVoltarClick: () -> Unit,
    onTentarNovamente: () -> Unit,
    onMudarEstagio: (TransicaoEstagio, RascunhoAplicacao?, String?) -> Unit,
    onConfirmarCiencia: () -> Unit,
    onAlternarEtapa: (Etapa) -> Unit,
    onMudarEstagioMensagemMostrada: () -> Unit,
    onAcaoMensagemMostrada: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // O Snackbar roda fora do LaunchedEffect: limpar o estado reinicia o efeito e cancelaria a exibição
    val escopo = rememberCoroutineScope()
    val hoje = remember { LocalDate.now() }
    var abaSelecionada by rememberSaveable { mutableIntStateOf(ABA_VISAO_GERAL) }
    // Saveable: o sheet reabre depois da rotação (o enum é Serializable)
    var transicaoAberta by rememberSaveable { mutableStateOf<TransicaoEstagio?>(null) }
    // Erro de gravação fica dentro do sheet: o Snackbar da tela seria desenhado atrás dele
    var erroMudarEstagio by rememberSaveable { mutableStateOf<String?>(null) }

    val mostrarMensagem: (String) -> Unit = { mensagem ->
        escopo.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(mensagem)
        }
    }

    LaunchedEffect(mudarEstagioUiState) {
        when (val estado = mudarEstagioUiState) {
            is MudarEstagioUiState.Sucesso -> {
                transicaoAberta = null
                erroMudarEstagio = null
                mostrarMensagem(estado.mensagem)
                onMudarEstagioMensagemMostrada()
            }
            is MudarEstagioUiState.EstagioMudou -> {
                transicaoAberta = null
                erroMudarEstagio = null
                mostrarMensagem(estado.mensagem)
                onMudarEstagioMensagemMostrada()
            }
            // O sheet continua aberto com o que foi digitado e mostra o erro acima dos botões
            is MudarEstagioUiState.Erro -> {
                erroMudarEstagio = estado.mensagem
                onMudarEstagioMensagemMostrada()
            }
            else -> Unit
        }
    }

    LaunchedEffect(acaoUiState) {
        when (val estado = acaoUiState) {
            is AcaoProjetoUiState.Sucesso -> {
                mostrarMensagem(estado.mensagem)
                onAcaoMensagemMostrada()
            }
            is AcaoProjetoUiState.Erro -> {
                mostrarMensagem(estado.mensagem)
                onAcaoMensagemMostrada()
            }
            AcaoProjetoUiState.Ocioso -> Unit
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Projeto",
                navigationIcon = {
                    IconButton(onClick = onVoltarClick) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            ) {
                // Editar projeto fica para a feature de edição
                IconButton(onClick = { mostrarMensagem("Disponível em breve") }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Mais opções",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (detalhesUiState) {
            is DetalhesProjetoUiState.Carregando -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is DetalhesProjetoUiState.Erro -> {
                Box(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), contentAlignment = Alignment.Center) {
                    ErrorRetry(message = detalhesUiState.mensagem, onRetry = onTentarNovamente)
                }
            }

            is DetalhesProjetoUiState.NaoEncontrado -> {
                Box(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), contentAlignment = Alignment.Center) {
                    ErrorRetry(message = detalhesUiState.mensagem, onRetry = onVoltarClick, actionLabel = "Voltar")
                }
            }

            is DetalhesProjetoUiState.Sucesso -> {
                val detalhes = detalhesUiState.detalhes
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "resumo") {
                        CardResumo(
                            detalhes = detalhes,
                            usuario = usuario,
                            hoje = hoje,
                            onTransicaoEscolhida = { transicaoAberta = it },
                            onConfirmarCiencia = onConfirmarCiencia
                        )
                    }
                    item(key = "abas") {
                        SegmentedTabs(
                            items = listOf("Visão geral", "Diário", "Arquivos"),
                            selectedIndex = abaSelecionada,
                            onSelect = { abaSelecionada = it },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                    if (abaSelecionada == ABA_VISAO_GERAL) {
                        if (detalhes.somenteLeitura) {
                            item(key = "somente-leitura") {
                                AlertBanner(
                                    title = "Projeto finalizado: somente leitura",
                                    message = "Para voltar a editar, o orientador precisa retornar o estágio.",
                                    icon = Icons.Outlined.Lock,
                                    containerColor = InfoContainerLight,
                                    contentColor = StatusTone.Blue.onContainer,
                                    iconSize = 18.dp
                                )
                            }
                        }
                        item(key = "descricao") { CardDescricao(projeto = detalhes.projeto) }
                        item(key = "participantes") { CardParticipantes(resumo = detalhes.resumo) }
                        item(key = "cronograma") {
                            CardCronograma(
                                etapas = detalhes.resumo.etapas,
                                hoje = hoje,
                                somenteLeitura = detalhes.somenteLeitura,
                                podeMarcar = detalhes.podeMarcarEtapa(usuario),
                                onAlternarEtapa = onAlternarEtapa
                            )
                        }
                        item(key = "historico") { CardHistorico(historico = detalhes.historico) }
                    } else {
                        item(key = "em-breve") { AbaEmBreve() }
                    }
                }

                transicaoAberta?.let { transicao ->
                    MudarEstagioBottomSheet(
                        transicao = transicao,
                        detalhes = detalhes,
                        salvando = mudarEstagioUiState is MudarEstagioUiState.Salvando,
                        erro = erroMudarEstagio,
                        onConfirmar = { aplicacao, justificativa ->
                            erroMudarEstagio = null
                            onMudarEstagio(transicao, aplicacao, justificativa)
                        },
                        onDispensar = {
                            transicaoAberta = null
                            erroMudarEstagio = null
                        }
                    )
                }
            }
        }
    }
}

// ===== Card de resumo (frame card do topo de 34:2) =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardResumo(
    detalhes: ProjetoDetalhes,
    usuario: Usuario,
    hoje: LocalDate,
    onTransicaoEscolhida: (TransicaoEstagio) -> Unit,
    onConfirmarCiencia: () -> Unit
) {
    val resumo = detalhes.resumo
    val projeto = detalhes.projeto
    val transicoes = detalhes.transicoesPara(usuario)
    val podeConfirmarCiencia = detalhes.podeConfirmarCiencia(usuario)
    var menuAberto by remember { mutableStateOf(false) }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "GRUPO ${projeto.nomeGrupo.uppercase()}",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = projeto.titulo,
                style = MaterialTheme.typography.titleLarge.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(label = projeto.estagio.titulo, tone = projeto.estagio.tom())
                if (resumo.atrasado(hoje)) {
                    ChipComIcone("Atrasado", StatusTone.Red, Icons.Outlined.Schedule)
                }
                if (projeto.aguardandoCienciaOrientador) {
                    ChipComIcone("Aguardando ciência do orientador", StatusTone.Purple, Icons.Outlined.Visibility)
                }
            }
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinhaInfo(Icons.Outlined.PersonOutline, "Orientação: ", resumo.orientador.nome)
                periodo(projeto.dataInicio, projeto.dataFimPrevisto) { it.formatarData() }?.let {
                    LinhaInfo(Icons.Outlined.CalendarToday, "Projeto: ", it)
                }
                periodo(projeto.aplicacaoInicio, projeto.aplicacaoFim) { it.formatarDiaMes() }?.let {
                    LinhaInfo(Icons.Outlined.Schedule, "Aplicação: ", it)
                }
            }
            if (transicoes.isNotEmpty() || podeConfirmarCiencia) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (transicoes.isNotEmpty()) {
                        // Uma transição abre o sheet direto; duas abrem o menu suspenso (decisão da spec)
                        Box(Modifier.weight(1f)) {
                            SecondaryButton(
                                text = "Mudar estágio",
                                onClick = {
                                    if (transicoes.size == 1) onTransicaoEscolhida(transicoes.first()) else menuAberto = true
                                },
                                leadingIcon = Icons.Outlined.OpenWith,
                                accent = true,
                                compact = true
                            )
                            DropdownMenu(
                                expanded = menuAberto,
                                onDismissRequest = { menuAberto = false },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                transicoes.forEach { transicao ->
                                    DropdownMenuItem(
                                        text = { Text(transicao.rotuloMenu, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            menuAberto = false
                                            onTransicaoEscolhida(transicao)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (podeConfirmarCiencia) {
                        PrimaryButton(
                            text = "Confirmar ciência",
                            onClick = onConfirmarCiencia,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.Check,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

// "Orientação: **Ana Ribeiro**": rótulo normal, valor em negrito preto
@Composable
private fun LinhaInfo(icone: ImageVector, rotulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = buildAnnotatedString {
                append(rotulo)
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)) {
                    append(valor)
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// "01/09/2026 a 15/12/2026"; null se faltar alguma data ou ela for inválida
private fun periodo(inicio: String?, fim: String?, formatar: (LocalDate) -> String): String? {
    val dataInicio = lerDataIso(inicio) ?: return null
    val dataFim = lerDataIso(fim) ?: return null
    return "${formatar(dataInicio)} a ${formatar(dataFim)}"
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun ProjetoDetalhesScreenPreview() {
    val ana = Usuario(id = 1, nome = "Ana Ribeiro", perfil = PerfilUsuario.PROFESSOR)
    val mateus = Usuario(id = 2, nome = "Mateus Silva")
    val hoje = LocalDate.now()
    val detalhes = ProjetoDetalhes(
        resumo = ProjetoResumo(
            projeto = Projeto(
                id = 1,
                titulo = "Horta comunitária na Escola Municipal Vila Nova",
                nomeGrupo = "Raízes",
                descricao = "Implantação de uma horta comunitária com os alunos do 4º e 5º ano, trabalhando " +
                    "alimentação saudável, ciclo das plantas e cuidado coletivo com o espaço da escola.",
                tema = TemaProjeto.MEIO_AMBIENTE,
                orientadorId = 1,
                dataInicio = hoje.minusDays(60).toString(),
                dataFimPrevisto = hoje.plusDays(40).toString(),
                estagio = EstagioProjeto.EM_APLICACAO,
                aplicacaoInicio = hoje.minusDays(20).toString(),
                aplicacaoFim = hoje.minusDays(2).toString(),
                aguardandoCienciaOrientador = true
            ),
            orientador = ana,
            participantes = listOf(mateus, Usuario(id = 3, nome = "Júlia Lima")),
            etapas = listOf(
                Etapa(id = 1, nome = "Planejamento", dataPrevista = hoje.minusDays(40).toString(), concluida = true),
                Etapa(id = 2, nome = "Relatório final", dataPrevista = hoje.minusDays(5).toString(), ordem = 1),
                Etapa(id = 3, nome = "Apresentação", dataPrevista = hoje.plusDays(30).toString(), ordem = 2)
            )
        ),
        historico = listOf(
            EventoHistorico(
                HistoricoEstagio(estagio = EstagioProjeto.EM_APLICACAO, data = Instant.now().toString()),
                autor = mateus,
                estagioAnterior = EstagioProjeto.EM_ANALISE
            ),
            EventoHistorico(HistoricoEstagio(estagio = EstagioProjeto.EM_ANALISE, data = Instant.now().toString()), autor = mateus)
        )
    )
    ExtensionConnectAPPTheme(darkTheme = false) {
        ProjetoDetalhesScreen(
            usuario = ana,
            detalhesUiState = DetalhesProjetoUiState.Sucesso(detalhes),
            mudarEstagioUiState = MudarEstagioUiState.Ocioso,
            acaoUiState = AcaoProjetoUiState.Ocioso,
            onVoltarClick = {},
            onTentarNovamente = {},
            onMudarEstagio = { _, _, _ -> },
            onConfirmarCiencia = {},
            onAlternarEtapa = {},
            onMudarEstagioMensagemMostrada = {},
            onAcaoMensagemMostrada = {}
        )
    }
}
