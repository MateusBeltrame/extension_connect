package br.com.mateushb.extensionconnectapp.ui.telas

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FileUpload
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
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Arquivo
import br.com.mateushb.extensionconnectapp.model.Etapa
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.EventoHistorico
import br.com.mateushb.extensionconnectapp.model.FiltroDiario
import br.com.mateushb.extensionconnectapp.model.FormatoDocumento
import br.com.mateushb.extensionconnectapp.model.HistoricoEstagio
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.model.ProjetoDetalhes
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.RascunhoAplicacao
import br.com.mateushb.extensionconnectapp.model.RegistroDiario
import br.com.mateushb.extensionconnectapp.model.RegistroDiarioDetalhes
import br.com.mateushb.extensionconnectapp.model.TemaProjeto
import br.com.mateushb.extensionconnectapp.model.TipoRegistro
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
import br.com.mateushb.extensionconnectapp.ui.state.EnvioArquivoUiState
import br.com.mateushb.extensionconnectapp.ui.state.ExclusaoArquivoUiState
import br.com.mateushb.extensionconnectapp.ui.state.ExclusaoRegistroDiarioUiState
import br.com.mateushb.extensionconnectapp.ui.state.ListaArquivosUiState
import br.com.mateushb.extensionconnectapp.ui.state.ListaRegistrosDiarioUiState
import br.com.mateushb.extensionconnectapp.ui.state.MudarEstagioUiState
import br.com.mateushb.extensionconnectapp.ui.state.PreparacaoFotoUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarRegistroDiarioUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import br.com.mateushb.extensionconnectapp.ui.theme.InfoContainerLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

private const val ABA_VISAO_GERAL = 0
private const val ABA_DIARIO = 1
private const val ABA_ARQUIVOS = 2

// Detalhes do projeto (spec 004): card de resumo com as ações de estágio, abas e aba Visão geral.
// Frames 34:2 (orientador) e 34:220 (finalizado, somente leitura). Aba Diário na spec 005 (frames 35:2
// e 35:100): o cabeçalho mostra o título do projeto e o card de resumo sai, como no Figma. Aba Arquivos
// na spec 006 (frames 35:231, 35:408 e 35:630), com o mesmo cabeçalho do Diário
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
    onAcaoMensagemMostrada: () -> Unit,
    listaRegistrosUiState: ListaRegistrosDiarioUiState,
    salvarRegistroUiState: SalvarRegistroDiarioUiState,
    exclusaoRegistroUiState: ExclusaoRegistroDiarioUiState,
    onPublicarRegistro: (texto: String, etapaId: Int?) -> Unit,
    onExcluirRegistro: (registroId: Int) -> Unit,
    onTentarNovamenteDiario: () -> Unit,
    onSalvarRegistroMensagemMostrada: () -> Unit,
    onExclusaoRegistroMensagemMostrada: () -> Unit,
    listaArquivosUiState: ListaArquivosUiState,
    preparacaoFotoUiState: PreparacaoFotoUiState,
    envioDocumentoUiState: EnvioArquivoUiState,
    envioFotoUiState: EnvioArquivoUiState,
    exclusaoArquivoUiState: ExclusaoArquivoUiState,
    onEnviarDocumento: (Uri) -> Unit,
    onPrepararFoto: (uri: Uri, daCamera: Boolean) -> Unit,
    onEnviarFoto: (autorizada: Boolean) -> Unit,
    onDescartarFoto: () -> Unit,
    onAlterarAutorizacaoMural: (fotoId: Int, autorizada: Boolean) -> Unit,
    onExcluirDocumento: (arquivoId: Int) -> Unit,
    onExcluirFoto: (fotoId: Int) -> Unit,
    // Os seletores do sistema ficam na tela; as Uris vêm do ViewModel (exceção do plan, Artigo 5.3)
    novaUriCaptura: () -> Uri?,
    uriDoDocumento: (Arquivo) -> Uri?,
    onTentarNovamenteArquivos: () -> Unit,
    onPreparacaoFotoErroMostrado: () -> Unit,
    onEnvioDocumentoMensagemMostrada: () -> Unit,
    onEnvioFotoMensagemMostrada: () -> Unit,
    onExclusaoArquivoMensagemMostrada: () -> Unit
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
    // Diário (spec 005): filtro, sheet e diálogo sobrevivem à rotação
    var filtroDiario by rememberSaveable { mutableStateOf(FiltroDiario.TODOS) }
    var novoRegistroAberto by rememberSaveable { mutableStateOf(false) }
    var erroNovoRegistro by rememberSaveable { mutableStateOf<String?>(null) }
    var registroParaExcluir by rememberSaveable { mutableStateOf<Int?>(null) }
    // Arquivos (spec 006): sheets, diálogo e a Uri da câmera sobrevivem à rotação
    var enviarArquivoAberto by rememberSaveable { mutableStateOf(false) }
    var uriCaptura by rememberSaveable { mutableStateOf<String?>(null) }
    var fotoAbertaId by rememberSaveable { mutableStateOf<Int?>(null) }
    var documentoParaExcluir by rememberSaveable { mutableStateOf<Int?>(null) }
    var erroEnvioFoto by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val contexto = LocalContext.current

    val detalhesCarregados = (detalhesUiState as? DetalhesProjetoUiState.Sucesso)?.detalhes
    val noDiario = abaSelecionada == ABA_DIARIO && detalhesCarregados != null
    val noArquivos = abaSelecionada == ABA_ARQUIVOS && detalhesCarregados != null
    // Diário e Arquivos: título do projeto no cabeçalho e sem o card de resumo (frames 35:2 e 35:231)
    val cabecalhoDoProjeto = noDiario || noArquivos
    val tipoRegistro = detalhesCarregados?.tipoRegistroPara(usuario)
    val podeEnviarArquivos = detalhesCarregados?.podeEnviarArquivos(usuario) == true
    val arquivosCarregados = (listaArquivosUiState as? ListaArquivosUiState.Sucesso)?.arquivos
    // "Hoje"/"Ontem": recalculados a cada emissão do diário e na virada do dia, com a tela aberta
    var agora by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(listaRegistrosUiState) { agora = LocalDateTime.now() }
    LaunchedEffect(Unit) {
        while (true) {
            val agoraReal = LocalDateTime.now()
            val meiaNoite = agoraReal.toLocalDate().plusDays(1).atStartOfDay()
            delay(Duration.between(agoraReal, meiaNoite).toMillis() + 1_000)
            agora = LocalDateTime.now()
        }
    }

    val mostrarMensagem: (String) -> Unit = { mensagem ->
        escopo.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(mensagem)
        }
    }

    // ===== Arquivos: câmera, seletores do sistema e abrir documento =====

    val tirarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { gravou ->
        val uri = uriCaptura
        uriCaptura = null
        if (gravou && uri != null) onPrepararFoto(Uri.parse(uri), true)
    }
    val escolherFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onPrepararFoto(it, false) }
    }
    val escolherDocumento = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onEnviarDocumento)
    }
    val abrirCamera: () -> Unit = {
        val uri = novaUriCaptura()
        if (uri == null) {
            mostrarMensagem("Nenhum app de câmera disponível.")
        } else {
            uriCaptura = uri.toString()
            try {
                tirarFoto.launch(uri)
            } catch (_: ActivityNotFoundException) {
                uriCaptura = null
                mostrarMensagem("Nenhum app de câmera disponível.")
            }
        }
    }
    val abrirDocumento: (Arquivo) -> Unit = { arquivo ->
        val uri = uriDoDocumento(arquivo)
        if (uri == null) {
            mostrarMensagem("Arquivo não encontrado neste aparelho.")
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, arquivo.extensao.mime)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                contexto.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                mostrarMensagem("Nenhum app para abrir este arquivo.")
            }
        }
    }

    LaunchedEffect(preparacaoFotoUiState) {
        val estado = preparacaoFotoUiState
        if (estado is PreparacaoFotoUiState.Erro) {
            mostrarMensagem(estado.mensagem)
            onPreparacaoFotoErroMostrado()
        }
    }

    // Documento: o resultado sempre vira Snackbar (não há sheet aberto para ele)
    LaunchedEffect(envioDocumentoUiState) {
        when (val estado = envioDocumentoUiState) {
            is EnvioArquivoUiState.Sucesso -> {
                mostrarMensagem(estado.mensagem)
                onEnvioDocumentoMensagemMostrada()
            }
            is EnvioArquivoUiState.Erro -> {
                mostrarMensagem(estado.mensagem)
                onEnvioDocumentoMensagemMostrada()
            }
            is EnvioArquivoUiState.SemPermissao -> {
                enviarArquivoAberto = false
                mostrarMensagem(estado.mensagem)
                onEnvioDocumentoMensagemMostrada()
            }
            else -> Unit
        }
    }

    // Foto: com o sheet de autorização aberto, o erro fica nele (o Snackbar ficaria atrás do sheet)
    LaunchedEffect(envioFotoUiState) {
        when (val estado = envioFotoUiState) {
            is EnvioArquivoUiState.Sucesso -> {
                erroEnvioFoto = null
                mostrarMensagem(estado.mensagem)
                onEnvioFotoMensagemMostrada()
            }
            is EnvioArquivoUiState.SemPermissao -> {
                erroEnvioFoto = null
                enviarArquivoAberto = false
                mostrarMensagem(estado.mensagem)
                onEnvioFotoMensagemMostrada()
            }
            is EnvioArquivoUiState.Erro -> {
                if (preparacaoFotoUiState is PreparacaoFotoUiState.Pronta) {
                    erroEnvioFoto = estado.mensagem
                } else {
                    mostrarMensagem(estado.mensagem)
                }
                onEnvioFotoMensagemMostrada()
            }
            else -> Unit
        }
    }

    LaunchedEffect(exclusaoArquivoUiState) {
        when (val estado = exclusaoArquivoUiState) {
            is ExclusaoArquivoUiState.Sucesso -> {
                fotoAbertaId = null
                mostrarMensagem(estado.mensagem)
                onExclusaoArquivoMensagemMostrada()
            }
            is ExclusaoArquivoUiState.Erro -> {
                mostrarMensagem(estado.mensagem)
                onExclusaoArquivoMensagemMostrada()
            }
            else -> Unit
        }
    }

    // Foto aberta que sumiu da lista (excluída, inclusive em outro aparelho): fecha o sheet
    LaunchedEffect(arquivosCarregados, fotoAbertaId) {
        val id = fotoAbertaId ?: return@LaunchedEffect
        if (arquivosCarregados != null && arquivosCarregados.fotosEnviadas.none { it.foto.id == id }) fotoAbertaId = null
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

    LaunchedEffect(salvarRegistroUiState) {
        when (val estado = salvarRegistroUiState) {
            is SalvarRegistroDiarioUiState.Sucesso -> {
                novoRegistroAberto = false
                erroNovoRegistro = null
                // O novo registro aparece no topo da lista em Todos (HU-03). A rolagem roda à parte: um
                // toque na lista a cancela, e isso não pode engolir a mensagem nem prender o estado
                filtroDiario = FiltroDiario.TODOS
                mostrarMensagem(estado.mensagem)
                onSalvarRegistroMensagemMostrada()
                escopo.launch { listState.animateScrollToItem(0) }
            }
            is SalvarRegistroDiarioUiState.SemPermissao -> {
                novoRegistroAberto = false
                erroNovoRegistro = null
                mostrarMensagem(estado.mensagem)
                onSalvarRegistroMensagemMostrada()
            }
            // O sheet continua aberto com o que foi digitado e mostra o erro acima dos botões
            is SalvarRegistroDiarioUiState.Erro -> {
                erroNovoRegistro = estado.mensagem
                onSalvarRegistroMensagemMostrada()
            }
            else -> Unit
        }
    }

    LaunchedEffect(exclusaoRegistroUiState) {
        when (val estado = exclusaoRegistroUiState) {
            is ExclusaoRegistroDiarioUiState.Sucesso -> {
                mostrarMensagem(estado.mensagem)
                onExclusaoRegistroMensagemMostrada()
            }
            is ExclusaoRegistroDiarioUiState.Erro -> {
                mostrarMensagem(estado.mensagem)
                onExclusaoRegistroMensagemMostrada()
            }
            else -> Unit
        }
    }

    // O projeto foi finalizado (ou o usuário saiu dele) com o sheet aberto: fecha e avisa. Durante a
    // gravação espera o resultado, que já diz se publicou ou não (evita duas mensagens contraditórias)
    val salvandoRegistro = salvarRegistroUiState is SalvarRegistroDiarioUiState.Salvando
    LaunchedEffect(detalhesCarregados != null, tipoRegistro, salvandoRegistro) {
        if (detalhesCarregados != null && tipoRegistro == null && novoRegistroAberto && !salvandoRegistro) {
            novoRegistroAberto = false
            erroNovoRegistro = null
            mostrarMensagem("Você não pode mais publicar neste projeto.")
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (cabecalhoDoProjeto) detalhesCarregados.projeto.titulo else "Projeto",
                subtitle = if (cabecalhoDoProjeto) {
                    "Grupo ${detalhesCarregados.projeto.nomeGrupo} · ${detalhesCarregados.projeto.estagio.titulo}"
                } else {
                    null
                },
                compactTitle = cabecalhoDoProjeto,
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
        floatingActionButton = {
            // Só na aba Diário e para quem publica (participante ou orientador, projeto não finalizado)
            if (noDiario && tipoRegistro != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        erroNovoRegistro = null
                        novoRegistroAberto = true
                    },
                    icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    text = {
                        Text(
                            text = "Novo registro",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            // Só na aba Arquivos e para quem envia (participante ou orientador, projeto não finalizado)
            if (noArquivos && podeEnviarArquivos) {
                ExtendedFloatingActionButton(
                    onClick = { enviarArquivoAberto = true },
                    icon = { Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    text = {
                        Text(
                            text = "Enviar",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
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
                    state = listState,
                    // No Diário e em Arquivos, o último card rola até ficar acima do FAB (56dp + margens)
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = if (cabecalhoDoProjeto) 12.dp else 16.dp,
                        bottom = if ((noDiario && tipoRegistro != null) || (noArquivos && podeEnviarArquivos)) 96.dp else 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (abaSelecionada == ABA_VISAO_GERAL) {
                        item(key = "resumo") {
                            CardResumo(
                                detalhes = detalhes,
                                usuario = usuario,
                                hoje = hoje,
                                onTransicaoEscolhida = { transicaoAberta = it },
                                onConfirmarCiencia = onConfirmarCiencia
                            )
                        }
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
                    } else if (abaSelecionada == ABA_DIARIO) {
                        abaDiario(
                            listaUiState = listaRegistrosUiState,
                            filtro = filtroDiario,
                            podePublicar = tipoRegistro != null,
                            agora = agora,
                            podeExcluir = { detalhes.podeExcluirRegistro(usuario, it.registro) },
                            onFiltroChange = { filtroDiario = it },
                            onExcluirClick = { registroParaExcluir = it.registro.id },
                            onTentarNovamente = onTentarNovamenteDiario
                        )
                    } else {
                        abaArquivos(
                            listaUiState = listaArquivosUiState,
                            usuarioId = usuario.id,
                            hoje = hoje,
                            podeExcluir = { autorId -> detalhes.podeGerenciarArquivo(usuario, autorId) },
                            onFotoClick = { fotoAbertaId = it.foto.id },
                            onAbrirDocumento = { abrirDocumento(it.arquivo) },
                            onExcluirDocumento = { documentoParaExcluir = it.arquivo.id },
                            onTentarNovamente = onTentarNovamenteArquivos
                        )
                    }
                }

                if (enviarArquivoAberto && podeEnviarArquivos) {
                    EnviarArquivoBottomSheet(
                        onTirarFoto = abrirCamera,
                        onEscolherFoto = {
                            escolherFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onEscolherDocumento = {
                            escolherDocumento.launch(FormatoDocumento.entries.map { it.mime }.toTypedArray())
                        },
                        onDispensar = { enviarArquivoAberto = false }
                    )
                }

                // Aberto enquanto comprime (prévia com indicador) e depois com a foto pronta
                val fotoPreparada = (preparacaoFotoUiState as? PreparacaoFotoUiState.Pronta)?.foto
                if (fotoPreparada != null || preparacaoFotoUiState is PreparacaoFotoUiState.Preparando) {
                    AutorizarDivulgacaoBottomSheet(
                        foto = fotoPreparada,
                        enviando = envioFotoUiState is EnvioArquivoUiState.Enviando,
                        erro = erroEnvioFoto,
                        onEnviar = { autorizada ->
                            erroEnvioFoto = null
                            onEnviarFoto(autorizada)
                        },
                        onDispensar = {
                            erroEnvioFoto = null
                            onDescartarFoto()
                        }
                    )
                }

                val fotoAberta = fotoAbertaId?.let { id -> arquivosCarregados?.fotosEnviadas?.firstOrNull { it.foto.id == id } }
                if (fotoAberta != null) {
                    FotoBottomSheet(
                        detalhes = fotoAberta,
                        hoje = hoje,
                        podeGerenciar = detalhes.podeGerenciarArquivo(usuario, fotoAberta.foto.autorId),
                        onAlterarAutorizacao = { onAlterarAutorizacaoMural(fotoAberta.foto.id, it) },
                        onExcluir = { onExcluirFoto(fotoAberta.foto.id) },
                        onDispensar = { fotoAbertaId = null }
                    )
                }

                documentoParaExcluir?.let { arquivoId ->
                    val nome = arquivosCarregados?.documentos?.firstOrNull { it.arquivo.id == arquivoId }?.arquivo?.nomeArquivo.orEmpty()
                    ConfirmacaoExclusaoArquivoDialog(
                        texto = "Deseja realmente excluir o documento \"$nome\"?",
                        onConfirmar = {
                            documentoParaExcluir = null
                            onExcluirDocumento(arquivoId)
                        },
                        onDispensar = { documentoParaExcluir = null }
                    )
                }

                if (novoRegistroAberto && tipoRegistro != null) {
                    NovoRegistroBottomSheet(
                        tipo = tipoRegistro,
                        etapas = detalhes.resumo.etapas,
                        salvando = salvarRegistroUiState is SalvarRegistroDiarioUiState.Salvando,
                        erro = erroNovoRegistro,
                        onPublicar = { texto, etapaId ->
                            erroNovoRegistro = null
                            onPublicarRegistro(texto, etapaId)
                        },
                        onDispensar = {
                            novoRegistroAberto = false
                            erroNovoRegistro = null
                        }
                    )
                }

                registroParaExcluir?.let { registroId ->
                    ConfirmacaoExclusaoRegistroDialog(
                        onConfirmar = {
                            registroParaExcluir = null
                            onExcluirRegistro(registroId)
                        },
                        onDispensar = { registroParaExcluir = null }
                    )
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
            onAcaoMensagemMostrada = {},
            listaRegistrosUiState = ListaRegistrosDiarioUiState.Sucesso(
                listOf(
                    RegistroDiarioDetalhes(
                        registro = RegistroDiario(
                            id = 1,
                            projetoId = 1,
                            autorId = 1,
                            data = Instant.now().toString(),
                            tipo = TipoRegistro.ORIENTACAO,
                            texto = "Lembrem de registrar fotos autorizadas das oficinas e começar o relatório final até sexta."
                        ),
                        autor = ana,
                        etapa = detalhes.resumo.etapas[1]
                    )
                )
            ),
            salvarRegistroUiState = SalvarRegistroDiarioUiState.Ocioso,
            exclusaoRegistroUiState = ExclusaoRegistroDiarioUiState.Ocioso,
            onPublicarRegistro = { _, _ -> },
            onExcluirRegistro = {},
            onTentarNovamenteDiario = {},
            onSalvarRegistroMensagemMostrada = {},
            onExclusaoRegistroMensagemMostrada = {},
            listaArquivosUiState = ListaArquivosUiState.Carregando,
            preparacaoFotoUiState = PreparacaoFotoUiState.Ocioso,
            envioDocumentoUiState = EnvioArquivoUiState.Ocioso,
            envioFotoUiState = EnvioArquivoUiState.Ocioso,
            exclusaoArquivoUiState = ExclusaoArquivoUiState.Ocioso,
            onEnviarDocumento = {},
            onPrepararFoto = { _, _ -> },
            onEnviarFoto = {},
            onDescartarFoto = {},
            onAlterarAutorizacaoMural = { _, _ -> },
            onExcluirDocumento = {},
            onExcluirFoto = {},
            novaUriCaptura = { null },
            uriDoDocumento = { null },
            onTentarNovamenteArquivos = {},
            onPreparacaoFotoErroMostrado = {},
            onEnvioDocumentoMensagemMostrada = {},
            onEnvioFotoMensagemMostrada = {},
            onExclusaoArquivoMensagemMostrada = {}
        )
    }
}
