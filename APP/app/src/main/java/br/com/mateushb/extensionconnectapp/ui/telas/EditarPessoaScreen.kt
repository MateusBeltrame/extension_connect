package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.BloqueioEdicaoPessoa
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.PessoaDetalhes
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppDialog
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.AppTopBar
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.state.FormularioPessoaUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarPessoaUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber
import br.com.mateushb.extensionconnectapp.ui.theme.WarningContainerLight
import kotlinx.coroutines.launch

// Editar pessoa, só Administrador (spec 007, frames 36:175, 36:254, 36:348 e 36:420). usuario = quem está
// logado: no próprio cadastro o perfil e o switch Ativo ficam desabilitados (HU-05)
@Composable
fun EditarPessoaScreen(
    usuario: Usuario,
    formularioUiState: FormularioPessoaUiState,
    salvarUiState: SalvarPessoaUiState,
    onSalvarClick: (Usuario) -> Unit,
    onVoltarClick: () -> Unit,
    onTentarNovamente: () -> Unit,
    onSalvoComSucesso: (Usuario) -> Unit,
    onSalvarMensagemMostrada: () -> Unit,
    onRecarregarBloqueios: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    LaunchedEffect(salvarUiState) {
        when (salvarUiState) {
            is SalvarPessoaUiState.Sucesso -> onSalvoComSucesso(salvarUiState.usuario)
            is SalvarPessoaUiState.Erro -> {
                // Snackbar no escopo da tela: limpar o estado reinicia este LaunchedEffect
                escopo.launch { snackbarHostState.showSnackbar(salvarUiState.mensagem) }
                onSalvarMensagemMostrada()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Editar pessoa",
                navigationIcon = {
                    IconButton(onClick = onVoltarClick) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (formularioUiState) {
                is FormularioPessoaUiState.Ocioso, is FormularioPessoaUiState.Carregando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is FormularioPessoaUiState.Erro -> {
                    ErrorRetry(message = formularioUiState.mensagem, onRetry = onTentarNovamente, modifier = Modifier.fillMaxSize())
                }

                is FormularioPessoaUiState.Sucesso -> {
                    EditarPessoaConteudo(
                        detalhes = formularioUiState.detalhes,
                        proprioCadastro = formularioUiState.detalhes.usuario.id == usuario.id,
                        salvando = salvarUiState is SalvarPessoaUiState.Salvando,
                        bloqueioAoSalvar = (salvarUiState as? SalvarPessoaUiState.Bloqueado)?.bloqueio,
                        onSalvarClick = onSalvarClick,
                        onBloqueioAoSalvarDispensado = onSalvarMensagemMostrada,
                        onRecarregarBloqueios = onRecarregarBloqueios
                    )
                }
            }
        }
    }
}

// Diálogo local aberto pelo switch ou pela troca de perfil
private const val DIALOGO_DESATIVAR = "desativar"
private const val DIALOGO_ORIENTADOR = "orientador"
private const val DIALOGO_PARTICIPANTE = "participante"

@Composable
private fun EditarPessoaConteudo(
    detalhes: PessoaDetalhes,
    proprioCadastro: Boolean,
    salvando: Boolean,
    bloqueioAoSalvar: BloqueioEdicaoPessoa?,
    onSalvarClick: (Usuario) -> Unit,
    onBloqueioAoSalvarDispensado: () -> Unit,
    onRecarregarBloqueios: () -> Unit
) {
    val pessoa = detalhes.usuario
    // rememberSaveable: o que foi digitado (e o diálogo aberto) sobrevive à rotação
    var nome by rememberSaveable(pessoa.id) { mutableStateOf(pessoa.nome) }
    var perfil by rememberSaveable(pessoa.id) { mutableStateOf(pessoa.perfil) }
    var ra by rememberSaveable(pessoa.id) { mutableStateOf(pessoa.ra.orEmpty()) }
    var curso by rememberSaveable(pessoa.id) { mutableStateOf(pessoa.curso) }
    var ativo by rememberSaveable(pessoa.id) { mutableStateOf(pessoa.ativo) }
    var dialogo by rememberSaveable(pessoa.id) { mutableStateOf<String?>(null) }
    var nomeErro by rememberSaveable(pessoa.id) { mutableStateOf<String?>(null) }
    var raErro by rememberSaveable(pessoa.id) { mutableStateOf<String?>(null) }
    var perfilExpandido by remember { mutableStateOf(false) }

    // Abre o diálogo de bloqueio (10c ou a variação de participante) em vez de aplicar a mudança
    fun abrirBloqueio(bloqueio: BloqueioEdicaoPessoa) {
        dialogo = when (bloqueio) {
            is BloqueioEdicaoPessoa.OrientadorComProjetosAtivos -> DIALOGO_ORIENTADOR
            is BloqueioEdicaoPessoa.ParticipanteDeProjetoAtivo -> DIALOGO_PARTICIPANTE
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvatarPessoa(pessoa = pessoa, tamanho = 64)
                Text(
                    text = pessoa.nome,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                StatusChip(label = pessoa.perfil.titulo, tone = tomDoPerfil(pessoa.perfil))
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AppTextField(
                    value = nome,
                    onValueChange = { nome = it; nomeErro = null },
                    placeholder = "Nome e sobrenome",
                    label = "Nome completo",
                    leadingIcon = { IconeCampo(Icons.Outlined.PersonOutline) },
                    supportingText = nomeErro,
                    isError = nomeErro != null,
                    enabled = !salvando
                )
                AppTextField(
                    value = pessoa.email,
                    onValueChange = {},
                    placeholder = "",
                    label = "E-mail",
                    leadingIcon = { IconeCampo(Icons.Outlined.MailOutline) },
                    trailingIcon = { IconeCampo(Icons.Outlined.Lock) },
                    supportingText = "O e-mail não pode ser alterado",
                    enabled = false,
                    locked = true
                )
                Box {
                    AppTextField(
                        value = perfil.titulo,
                        onValueChange = {},
                        placeholder = "",
                        label = "Perfil",
                        leadingIcon = { IconeCampo(Icons.Outlined.Shield) },
                        trailingIcon = { IconeCampo(Icons.Outlined.KeyboardArrowDown) },
                        supportingText = if (proprioCadastro) "Você não pode alterar seu próprio perfil" else null,
                        enabled = !salvando,
                        locked = proprioCadastro,
                        onClick = { perfilExpandido = true }
                    )
                    DropdownMenu(
                        expanded = perfilExpandido,
                        onDismissRequest = { perfilExpandido = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        PerfilUsuario.entries.forEach { opcao ->
                            DropdownMenuItem(
                                text = { Text(text = opcao.titulo, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    perfilExpandido = false
                                    val bloqueio = detalhes.bloqueioPara(opcao, ativo)
                                    if (bloqueio != null) abrirBloqueio(bloqueio) else perfil = opcao
                                }
                            )
                        }
                    }
                }
                // RA e curso só para Aluno; opcionais (vazios = "Cadastro incompleto")
                if (perfil == PerfilUsuario.ALUNO) {
                    CamposAluno(
                        ra = ra,
                        onRaChange = { ra = it; raErro = null },
                        raErro = raErro,
                        curso = curso,
                        onCursoChange = { curso = it },
                        enabled = !salvando
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Ativo",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pessoas inativas não conseguem entrar no app, mas continuam nos históricos.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    SwitchMarca(
                        checked = ativo,
                        enabled = !proprioCadastro && !salvando,
                        onCheckedChange = { ligado ->
                            if (ligado) {
                                ativo = true
                            } else {
                                val bloqueio = detalhes.bloqueioPara(perfil, false)
                                if (bloqueio != null) {
                                    abrirBloqueio(bloqueio)
                                } else {
                                    // Desliga já (como no frame 36:254); "Cancelar" religa
                                    ativo = false
                                    dialogo = DIALOGO_DESATIVAR
                                }
                            }
                        }
                    )
                }
            }
        }

        PrimaryButton(
            text = "Salvar alterações",
            loading = salvando,
            onClick = {
                val aluno = perfil == PerfilUsuario.ALUNO
                nomeErro = if (nome.isBlank()) "Nome é obrigatório" else null
                // RA é opcional; se preenchido, só números
                raErro = if (aluno) ValidacaoAcesso.erroRa(ra, obrigatorio = false) else null
                if (nomeErro == null && raErro == null) {
                    onSalvarClick(
                        pessoa.copy(
                            nome = nome.trim(),
                            perfil = perfil,
                            ativo = ativo,
                            ra = ra.trim().takeIf { aluno && it.isNotEmpty() },
                            curso = curso.takeIf { aluno }
                        )
                    )
                }
            }
        )
    }

    when (dialogo) {
        DIALOGO_DESATIVAR -> ConfirmacaoDesativacaoPessoaDialog(
            pessoa = pessoa,
            onConfirmar = { dialogo = null },
            onDispensar = {
                ativo = true
                dialogo = null
            }
        )
        // Ao fechar, relê os vínculos: se um projeto foi finalizado desde que a tela abriu, a próxima
        // tentativa já não é bloqueada
        DIALOGO_ORIENTADOR -> BloqueioEdicaoPessoaDialog(
            nome = pessoa.nome,
            bloqueio = BloqueioEdicaoPessoa.OrientadorComProjetosAtivos(detalhes.projetosOrientadosAtivos),
            onDispensar = {
                dialogo = null
                onRecarregarBloqueios()
            }
        )
        DIALOGO_PARTICIPANTE -> BloqueioEdicaoPessoaDialog(
            nome = pessoa.nome,
            bloqueio = BloqueioEdicaoPessoa.ParticipanteDeProjetoAtivo(detalhes.grupoParticipacaoAtiva.orEmpty()),
            onDispensar = {
                dialogo = null
                onRecarregarBloqueios()
            }
        )
    }

    // Um projeto mudou depois que a tela abriu: o banco recusou e devolveu o bloqueio atual. Perfil e
    // Ativo voltam ao que está gravado (o resto do que foi digitado fica), para o próximo salvar não
    // repetir a mesma recusa
    if (bloqueioAoSalvar != null) {
        BloqueioEdicaoPessoaDialog(
            nome = pessoa.nome,
            bloqueio = bloqueioAoSalvar,
            onDispensar = {
                perfil = pessoa.perfil
                ativo = pessoa.ativo
                onBloqueioAoSalvarDispensado()
            }
        )
    }
}

// Frame 36:254. "Desativar" só confirma o switch; a gravação é em "Salvar alterações"
@Composable
private fun ConfirmacaoDesativacaoPessoaDialog(pessoa: Usuario, onConfirmar: () -> Unit, onDispensar: () -> Unit) {
    AppDialog(
        icon = Icons.Outlined.PersonOff,
        iconColor = MaterialTheme.colorScheme.error,
        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
        title = "Desativar ${pessoa.nome}?",
        text = "${pessoa.primeiroNome} não poderá mais entrar no app e some das buscas de participantes. " +
            "Os registros e históricos continuam nos projetos.",
        confirmText = "Desativar",
        onConfirm = onConfirmar,
        onDismiss = onDispensar,
        dismissText = "Cancelar",
        destructive = true
    )
}

// Frame 36:348 e a variação para aluno participante de projeto ativo (sem frame próprio)
@Composable
private fun BloqueioEdicaoPessoaDialog(nome: String, bloqueio: BloqueioEdicaoPessoa, onDispensar: () -> Unit) {
    val (titulo, texto) = when (bloqueio) {
        is BloqueioEdicaoPessoa.OrientadorComProjetosAtivos -> {
            val quantidade = if (bloqueio.titulos.size == 1) "1 projeto ativo" else "${bloqueio.titulos.size} projetos ativos"
            "Troque o orientador antes" to
                "$nome orienta $quantidade: ${juntarComE(bloqueio.titulos)}. Defina outro orientador nesses " +
                "projetos antes de mudar o perfil ou desativar."
        }
        is BloqueioEdicaoPessoa.ParticipanteDeProjetoAtivo ->
            "Remova o aluno do projeto antes" to
                "$nome participa de um projeto ativo: Grupo ${bloqueio.nomeGrupo}. É preciso sair do projeto " +
                "antes de mudar o perfil."
    }
    AppDialog(
        icon = Icons.Outlined.WarningAmber,
        iconColor = WarningAmber,
        iconContainerColor = WarningContainerLight,
        title = titulo,
        text = texto,
        confirmText = "Entendi",
        onConfirm = onDispensar,
        onDismiss = onDispensar
    )
}

// "A", "A e B", "A, B e C"
private fun juntarComE(itens: List<String>): String =
    if (itens.size <= 1) itens.joinToString() else itens.dropLast(1).joinToString(", ") + " e " + itens.last()

// ===== Previews =====

@Preview(showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun EditarPessoaScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        EditarPessoaScreen(
            usuario = Usuario(id = 99, nome = "Renata Alves", perfil = PerfilUsuario.ADMINISTRADOR),
            formularioUiState = FormularioPessoaUiState.Sucesso(
                PessoaDetalhes(
                    usuario = Usuario(
                        id = 3, nome = "Bruno Costa", email = "bruno.costa@afya.br",
                        ra = "2024118230", curso = Curso.ENGENHARIA_SOFTWARE
                    )
                )
            ),
            salvarUiState = SalvarPessoaUiState.Ocioso,
            onSalvarClick = {},
            onVoltarClick = {},
            onTentarNovamente = {},
            onSalvoComSucesso = {},
            onSalvarMensagemMostrada = {},
            onRecarregarBloqueios = {}
        )
    }
}
