package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.EstagioProjeto
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.model.dataCompleta
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.Avatar
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.components.GradientHeader
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SecondaryButton
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.state.ProjetosPerfilUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarPerfilUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import kotlinx.coroutines.launch

// Meu perfil (spec 008, frames 36:482 e 36:606): visualizar e editar na mesma rota; o modo editando é
// estado local. usuario = sessão (acompanha a linha no banco, então o nome salvo aparece sozinho).
// mensagem = Snackbar vindo de outra tela ("Senha alterada com sucesso")
@Composable
fun MeuPerfilScreen(
    usuario: Usuario,
    projetosUiState: ProjetosPerfilUiState,
    salvarUiState: SalvarPerfilUiState,
    versaoApp: String,
    mensagem: String?,
    onSalvarClick: (nome: String, ra: String?, curso: Curso?) -> Unit,
    onSalvoComSucesso: (Usuario) -> Unit,
    onProjetoClick: (Int) -> Unit,
    onTrocarSenhaClick: () -> Unit,
    onSairClick: () -> Unit,
    onTentarNovamente: () -> Unit,
    onSalvarMensagemMostrada: () -> Unit,
    onMensagemMostrada: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val aluno = usuario.perfil == PerfilUsuario.ALUNO
    val salvando = salvarUiState is SalvarPerfilUiState.Salvando

    // rememberSaveable: modo editando e o que foi digitado sobrevivem à rotação
    var editando by rememberSaveable { mutableStateOf(false) }
    var nome by rememberSaveable { mutableStateOf("") }
    var ra by rememberSaveable { mutableStateOf("") }
    var curso by rememberSaveable { mutableStateOf<Curso?>(null) }
    var nomeErro by rememberSaveable { mutableStateOf<String?>(null) }
    var raErro by rememberSaveable { mutableStateOf<String?>(null) }
    var cursoErro by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(salvarUiState) {
        when (salvarUiState) {
            is SalvarPerfilUiState.Sucesso -> {
                // A sessão recebe o usuário salvo antes de sair da edição: sem piscar o nome antigo
                onSalvoComSucesso(salvarUiState.usuario)
                editando = false
                // Snackbar no escopo da tela: limpar o estado reinicia este LaunchedEffect
                escopo.launch { snackbarHostState.showSnackbar("Dados atualizados com sucesso") }
                onSalvarMensagemMostrada()
            }
            is SalvarPerfilUiState.Erro -> {
                escopo.launch { snackbarHostState.showSnackbar(salvarUiState.mensagem) }
                onSalvarMensagemMostrada()
            }
            else -> Unit
        }
    }

    LaunchedEffect(mensagem) {
        if (mensagem != null) {
            escopo.launch { snackbarHostState.showSnackbar(mensagem) }
            onMensagemMostrada()
        }
    }

    // Voltar do sistema no modo editando equivale a "Cancelar"; enquanto salva não faz nada (como o botão
    // Cancelar, desabilitado), para o resultado — inclusive um erro — chegar com a edição aberta
    BackHandler(enabled = editando) { if (!salvando) editando = false }

    Scaffold(
        // Sem insets no Scaffold: o cabeçalho fica atrás da status bar e a BottomBar já dá o espaço de baixo
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
            ) {
                GradientHeader(
                    title = usuario.nomeCurto,
                    compact = true,
                    logo = {
                        Avatar(
                            initials = iniciais(usuario.nome),
                            size = 80.dp,
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    },
                    badge = {
                        StatusChip(
                            label = usuario.perfil.titulo,
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    }
                )

                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (editando) {
                        EditarMeusDadosCard(
                            usuario = usuario,
                            nome = nome,
                            onNomeChange = { nome = it; nomeErro = null },
                            nomeErro = nomeErro,
                            ra = ra,
                            onRaChange = { ra = it; raErro = null },
                            raErro = raErro,
                            curso = curso,
                            onCursoChange = { curso = it; cursoErro = null },
                            cursoErro = cursoErro,
                            salvando = salvando,
                            onCancelarClick = { editando = false },
                            onSalvarClick = {
                                nomeErro = if (nome.isBlank()) "Nome é obrigatório" else null
                                raErro = if (aluno) ValidacaoAcesso.erroRa(ra, obrigatorio = true) else null
                                cursoErro = if (aluno && curso == null) "Selecione o curso" else null
                                if (nomeErro == null && raErro == null && cursoErro == null) {
                                    onSalvarClick(nome, ra.takeIf { aluno }, curso.takeIf { aluno })
                                }
                            }
                        )
                    } else {
                        MeusDadosCard(
                            usuario = usuario,
                            onEditarClick = {
                                nome = usuario.nome
                                ra = usuario.ra.orEmpty()
                                curso = usuario.curso
                                nomeErro = null
                                raErro = null
                                cursoErro = null
                                editando = true
                            }
                        )
                        // Administrador não orienta nem participa: sem card de projetos
                        if (usuario.perfil != PerfilUsuario.ADMINISTRADOR) {
                            ProjetosPerfilCard(
                                aluno = aluno,
                                projetosUiState = projetosUiState,
                                onProjetoClick = onProjetoClick,
                                onTentarNovamente = onTentarNovamente
                            )
                        }
                        AtalhosPerfilCard(
                            termosAceitosEm = dataCompleta(usuario.aceitouTermosEm),
                            onTrocarSenhaClick = onTrocarSenhaClick
                        )
                        SecondaryButton(
                            text = "Sair da conta",
                            onClick = onSairClick,
                            leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                            destructive = true
                        )
                        Text(
                            text = "Versão $versaoApp",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            // Faixa atrás da status bar, na cor do topo do gradiente, só depois de rolar: o conteúdo não passa
            // por baixo dos ícones do sistema (onde também não recebe toque) e, parado, o gradiente fica inteiro
            if (scrollState.value > 0) Box(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// ===== Modo visualizar =====

@Composable
private fun MeusDadosCard(usuario: Usuario, onEditarClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TituloCard("Meus dados", Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onEditarClick)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Editar",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            LinhaDado(rotulo = "Nome completo", valor = usuario.nome)
            LinhaDado(rotulo = "E-mail", valor = usuario.email, travado = true)
            LinhaDado(rotulo = "Perfil", valor = usuario.perfil.titulo, travado = true)
            if (usuario.perfil == PerfilUsuario.ALUNO) {
                LinhaDado(rotulo = "RA / Matrícula", valor = usuario.ra.orEmpty().ifBlank { "—" })
                LinhaDado(rotulo = "Curso", valor = usuario.curso?.titulo ?: "—")
            }
        }
    }
}

@Composable
private fun LinhaDado(rotulo: String, valor: String, travado: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (travado) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Não editável",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Aluno: "Projeto atual" (no máximo um). Professor: "Projetos que oriento"
@Composable
private fun ProjetosPerfilCard(
    aluno: Boolean,
    projetosUiState: ProjetosPerfilUiState,
    onProjetoClick: (Int) -> Unit,
    onTentarNovamente: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TituloCard(if (aluno) "Projeto atual" else "Projetos que oriento")
            when (projetosUiState) {
                is ProjetosPerfilUiState.Carregando -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
                is ProjetosPerfilUiState.Erro -> {
                    ErrorRetry(message = projetosUiState.mensagem, onRetry = onTentarNovamente, modifier = Modifier.fillMaxWidth())
                }
                is ProjetosPerfilUiState.Sucesso -> {
                    if (projetosUiState.projetos.isEmpty()) {
                        Text(
                            text = if (aluno) {
                                "Você ainda não participa de um projeto em andamento."
                            } else {
                                "Você não orienta nenhum projeto em andamento."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            projetosUiState.projetos.forEach { projeto ->
                                ProjetoPerfilItem(projeto = projeto, onClick = { onProjetoClick(projeto.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjetoPerfilItem(projeto: Projeto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Grupo ${projeto.nomeGrupo}".uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = projeto.titulo,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            StatusChip(label = projeto.estagio.titulo, tone = projeto.estagio.tom())
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Abrir projeto",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

// Termos e Privacidade são só informativos (spec 008): sem toque e sem chevron
@Composable
private fun AtalhosPerfilCard(termosAceitosEm: String?, onTrocarSenhaClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            LinhaAtalho(icone = Icons.Outlined.Key, titulo = "Trocar senha", onClick = onTrocarSenhaClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LinhaAtalho(
                icone = Icons.Outlined.Description,
                titulo = "Termos de uso",
                subtitulo = termosAceitosEm?.let { "Aceitos em $it" }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LinhaAtalho(
                icone = Icons.Outlined.Shield,
                titulo = "Privacidade e exclusão de dados",
                subtitulo = "Fale com a coordenação: extensao@afya.br"
            )
        }
    }
}

@Composable
private fun LinhaAtalho(icone: ImageVector, titulo: String, subtitulo: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ===== Modo editando =====

@Composable
private fun EditarMeusDadosCard(
    usuario: Usuario,
    nome: String,
    onNomeChange: (String) -> Unit,
    nomeErro: String?,
    ra: String,
    onRaChange: (String) -> Unit,
    raErro: String?,
    curso: Curso?,
    onCursoChange: (Curso) -> Unit,
    cursoErro: String?,
    salvando: Boolean,
    onCancelarClick: () -> Unit,
    onSalvarClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TituloCard("Editar meus dados")
            AppTextField(
                value = nome,
                onValueChange = onNomeChange,
                placeholder = "Nome e sobrenome",
                label = "Nome completo",
                leadingIcon = { IconeCampo(Icons.Outlined.PersonOutline) },
                supportingText = nomeErro,
                isError = nomeErro != null,
                enabled = !salvando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            AppTextField(
                value = usuario.email,
                onValueChange = {},
                placeholder = "",
                label = "E-mail",
                leadingIcon = { IconeCampo(Icons.Outlined.MailOutline) },
                trailingIcon = { IconeCampo(Icons.Outlined.Lock) },
                enabled = false,
                locked = true
            )
            AppTextField(
                value = usuario.perfil.titulo,
                onValueChange = {},
                placeholder = "",
                label = "Perfil",
                leadingIcon = { IconeCampo(Icons.Outlined.Shield) },
                trailingIcon = { IconeCampo(Icons.Outlined.Lock) },
                supportingText = "Só a coordenação altera o perfil",
                enabled = false,
                locked = true
            )
            if (usuario.perfil == PerfilUsuario.ALUNO) {
                CamposAluno(
                    ra = ra,
                    onRaChange = onRaChange,
                    raErro = raErro,
                    curso = curso,
                    onCursoChange = onCursoChange,
                    enabled = !salvando,
                    cursoErro = cursoErro
                )
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Cancelar",
                    onClick = onCancelarClick,
                    enabled = !salvando,
                    fullWidth = false,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Salvar",
                    onClick = onSalvarClick,
                    loading = salvando,
                    fullWidth = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ===== Previews =====

private val usuarioPreview = Usuario(
    id = 1,
    nome = "Mateus Henrique Silva",
    email = "mateus.silva@afya.br",
    ra = "2023104587",
    curso = Curso.ENGENHARIA_SOFTWARE,
    aceitouTermosEm = "2026-03-02T12:00:00Z"
)

@Preview(showBackground = true, heightDp = 1100, widthDp = 390)
@Composable
private fun MeuPerfilScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        MeuPerfilScreen(
            usuario = usuarioPreview,
            projetosUiState = ProjetosPerfilUiState.Sucesso(
                listOf(
                    Projeto(
                        id = 1,
                        titulo = "Horta comunitária na Escola Municipal Vila Nova",
                        nomeGrupo = "Raízes",
                        estagio = EstagioProjeto.EM_APLICACAO
                    )
                )
            ),
            salvarUiState = SalvarPerfilUiState.Ocioso,
            versaoApp = "1.0",
            mensagem = null,
            onSalvarClick = { _, _, _ -> },
            onSalvoComSucesso = {},
            onProjetoClick = {},
            onTrocarSenhaClick = {},
            onSairClick = {},
            onTentarNovamente = {},
            onSalvarMensagemMostrada = {},
            onMensagemMostrada = {}
        )
    }
}
