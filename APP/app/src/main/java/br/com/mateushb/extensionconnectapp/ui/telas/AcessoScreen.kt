package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.ui.components.AlertBanner
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.BottomLink
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SegmentedTabs
import br.com.mateushb.extensionconnectapp.ui.state.CadastroUiState
import br.com.mateushb.extensionconnectapp.ui.state.LoginUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import br.com.mateushb.extensionconnectapp.ui.theme.SuccessGreen

private const val ABA_ENTRAR = 0
private const val ABA_CADASTRAR = 1

@Composable
fun AcessoScreen(
    loginUiState: LoginUiState,
    cadastroUiState: CadastroUiState,
    onEntrarClick: (email: String, senha: String) -> Unit,
    onCadastrarClick: (nome: String, email: String, senha: String) -> Unit,
    onEsqueceuSenhaClick: () -> Unit,
    onLoginErroDispensado: () -> Unit,
    onCadastroErroDispensado: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var abaSelecionada by rememberSaveable { mutableIntStateOf(ABA_ENTRAR) }

    // Erro inesperado de banco no cadastro: Snackbar (erros de validação ficam nos campos)
    LaunchedEffect(cadastroUiState) {
        if (cadastroUiState is CadastroUiState.Erro) {
            snackbarHostState.showSnackbar(cadastroUiState.mensagem)
            onCadastroErroDispensado()
        }
    }

    val operacaoEmAndamento = loginUiState is LoginUiState.Entrando || cadastroUiState is CadastroUiState.Cadastrando
    // Trocar de aba só dispensa avisos de erro; durante uma operação a troca fica bloqueada para o
    // botão não ser reabilitado com a requisição ainda em andamento
    val selecionarAba: (Int) -> Unit = { aba ->
        if (aba != abaSelecionada && !operacaoEmAndamento) {
            abaSelecionada = aba
            if (loginUiState is LoginUiState.Erro) onLoginErroDispensado()
            if (cadastroUiState is CadastroUiState.EmailJaCadastrado) onCadastroErroDispensado()
        }
    }

    AcessoLayout(snackbarHostState = snackbarHostState) {
        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SegmentedTabs(
                    items = listOf("Entrar", "Cadastrar-se"),
                    selectedIndex = abaSelecionada,
                    onSelect = selecionarAba
                )
                if (abaSelecionada == ABA_ENTRAR) {
                    EntrarConteudo(
                        loginUiState = loginUiState,
                        onEntrarClick = onEntrarClick,
                        onEsqueceuSenhaClick = onEsqueceuSenhaClick,
                        onCampoAlterado = {
                            if (loginUiState is LoginUiState.Erro) onLoginErroDispensado()
                        }
                    )
                } else {
                    CadastrarConteudo(
                        cadastroUiState = cadastroUiState,
                        onCadastrarClick = onCadastrarClick,
                        onEmailAlterado = {
                            if (cadastroUiState is CadastroUiState.EmailJaCadastrado) onCadastroErroDispensado()
                        }
                    )
                }
            }
        }
        if (abaSelecionada == ABA_ENTRAR) {
            BottomLink(prefix = "Novo por aqui?", action = "Criar conta", onClick = { selecionarAba(ABA_CADASTRAR) })
            // "Ver projetos concluídos" (Mural) fica oculto até a feature 9 (spec 001, fora de escopo)
        } else {
            BottomLink(prefix = "Já tem uma conta?", action = "Entrar", onClick = { selecionarAba(ABA_ENTRAR) })
        }
    }
}

// ===== Aba Entrar =====

@Composable
private fun ColumnScope.EntrarConteudo(
    loginUiState: LoginUiState,
    onEntrarClick: (String, String) -> Unit,
    onEsqueceuSenhaClick: () -> Unit,
    onCampoAlterado: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    // Senha em remember (não rememberSaveable): não pode ir em texto puro para o savedInstanceState
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var emailErro by remember { mutableStateOf<String?>(null) }
    var senhaErro by remember { mutableStateOf<String?>(null) }
    val entrando = loginUiState is LoginUiState.Entrando

    CabecalhoCard(titulo = "Bem-vindo de volta", subtitulo = "Identifique-se para acessar seus projetos")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppTextField(
            value = email,
            onValueChange = { email = it; emailErro = null; onCampoAlterado() },
            placeholder = "Digite seu e-mail",
            label = "E-mail",
            leadingIcon = { IconeCampo(Icons.Outlined.Email) },
            supportingText = emailErro,
            isError = emailErro != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        CampoSenha(
            valor = senha,
            onValorChange = { senha = it; senhaErro = null; onCampoAlterado() },
            rotulo = "Senha",
            visivel = senhaVisivel,
            onAlternarVisibilidade = { senhaVisivel = !senhaVisivel },
            erro = senhaErro,
            imeAction = ImeAction.Done
        )
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(
            text = "Esqueceu a senha?",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onEsqueceuSenhaClick)
        )
    }

    if (loginUiState is LoginUiState.Erro) {
        AlertBanner(
            message = loginUiState.mensagem,
            icon = if (loginUiState.contaDesativada) Icons.Outlined.PersonOff else Icons.Outlined.ErrorOutline
        )
    }

    PrimaryButton(
        text = "Acessar Portal",
        loading = entrando,
        onClick = {
            emailErro = if (email.isBlank()) "Informe o e-mail" else null
            senhaErro = if (senha.isEmpty()) "Informe a senha" else null
            if (emailErro == null && senhaErro == null) onEntrarClick(email, senha)
        }
    )
}

// ===== Aba Cadastrar-se =====

@Composable
private fun ColumnScope.CadastrarConteudo(
    cadastroUiState: CadastroUiState,
    onCadastrarClick: (String, String, String) -> Unit,
    onEmailAlterado: () -> Unit
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Senhas em remember (não rememberSaveable): não podem ir em texto puro para o savedInstanceState
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var aceitouTermos by rememberSaveable { mutableStateOf(false) }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmacaoVisivel by remember { mutableStateOf(false) }
    var mostrarTermos by remember { mutableStateOf(false) }

    var nomeErro by remember { mutableStateOf<String?>(null) }
    var emailErro by remember { mutableStateOf<String?>(null) }
    var senhaErro by remember { mutableStateOf<String?>(null) }
    var confirmacaoErro by remember { mutableStateOf<String?>(null) }
    var termosErro by remember { mutableStateOf<String?>(null) }

    val cadastrando = cadastroUiState is CadastroUiState.Cadastrando
    val emailDuplicado = cadastroUiState is CadastroUiState.EmailJaCadastrado
    val senhasConferem = senha.isNotEmpty() && senha == confirmacao

    CabecalhoCard(titulo = "Crie sua conta acadêmica", subtitulo = "Cadastre-se para gerenciar seus projetos")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppTextField(
            value = nome,
            onValueChange = { nome = it; nomeErro = null },
            placeholder = "Seu nome completo",
            label = "Nome completo",
            leadingIcon = { IconeCampo(Icons.Outlined.Person) },
            supportingText = nomeErro,
            isError = nomeErro != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        val emailMensagemErro = emailErro ?: if (emailDuplicado) "Este e-mail já está cadastrado" else null
        AppTextField(
            value = email,
            onValueChange = { email = it; emailErro = null; onEmailAlterado() },
            placeholder = "Digite seu e-mail",
            label = "E-mail",
            leadingIcon = { IconeCampo(Icons.Outlined.Email) },
            supportingText = emailMensagemErro,
            isError = emailMensagemErro != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        CampoSenha(
            valor = senha,
            onValorChange = { senha = it; senhaErro = null; confirmacaoErro = null },
            rotulo = "Senha",
            visivel = senhaVisivel,
            onAlternarVisibilidade = { senhaVisivel = !senhaVisivel },
            erro = senhaErro,
            ajuda = "Mínimo de ${ValidacaoAcesso.TAMANHO_MINIMO_SENHA} caracteres",
            imeAction = ImeAction.Next
        )
        CampoSenha(
            valor = confirmacao,
            onValorChange = { confirmacao = it; confirmacaoErro = null },
            rotulo = "Confirmar Senha",
            visivel = confirmacaoVisivel,
            onAlternarVisibilidade = { confirmacaoVisivel = !confirmacaoVisivel },
            erro = confirmacaoErro,
            confirmado = senhasConferem,
            imeAction = ImeAction.Done
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column {
            AceiteTermos(
                aceito = aceitouTermos,
                onAceitoChange = { aceitouTermos = it; termosErro = null },
                onTermosClick = { mostrarTermos = true }
            )
            termosErro?.let { mensagem ->
                Spacer(Modifier.height(6.dp))
                Text(text = mensagem, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.error)
            }
        }
        PrimaryButton(
            text = "Criar Conta",
            loading = cadastrando,
            onClick = {
                nomeErro = if (nome.isBlank()) "Nome é obrigatório" else null
                emailErro = ValidacaoAcesso.erroEmail(email)
                senhaErro = if (senha.length < ValidacaoAcesso.TAMANHO_MINIMO_SENHA) {
                    "A senha deve ter no mínimo ${ValidacaoAcesso.TAMANHO_MINIMO_SENHA} caracteres"
                } else null
                confirmacaoErro = if (confirmacao != senha) "As senhas não conferem" else null
                termosErro = if (!aceitouTermos) "Aceite os termos de uso para continuar" else null
                val valido = listOf(nomeErro, emailErro, senhaErro, confirmacaoErro, termosErro).all { it == null }
                if (valido) onCadastrarClick(nome, email, senha)
            }
        )
    }

    if (mostrarTermos) {
        TermosDeUsoDialog(onDispensar = { mostrarTermos = false })
    }
}

// ===== Composables auxiliares =====

@Composable
private fun CampoSenha(
    valor: String,
    onValorChange: (String) -> Unit,
    rotulo: String,
    visivel: Boolean,
    onAlternarVisibilidade: () -> Unit,
    erro: String?,
    imeAction: ImeAction,
    ajuda: String? = null,
    confirmado: Boolean = false
) {
    AppTextField(
        value = valor,
        onValueChange = onValorChange,
        placeholder = "••••••••",
        label = rotulo,
        leadingIcon = { IconeCampo(Icons.Outlined.Lock) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Área de toque de 32dp; o offset mantém o ícone a 12dp da borda como no Figma
                Box(
                    modifier = Modifier
                        .offset(x = if (confirmado) 0.dp else 7.dp)
                        .size(32.dp)
                        .clickable(onClick = onAlternarVisibilidade),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (visivel) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (visivel) "Ocultar senha" else "Mostrar senha",
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (confirmado) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Senhas conferem",
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        visualTransformation = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
        supportingText = erro ?: ajuda,
        isError = erro != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction)
    )
}

@Composable
private fun AceiteTermos(
    aceito: Boolean,
    onAceitoChange: (Boolean) -> Unit,
    onTermosClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Checkbox(
                checked = aceito,
                onCheckedChange = onAceitoChange,
                modifier = Modifier.size(18.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Text(
            text = "  Aceito os ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onAceitoChange(!aceito) }
        )
        Text(
            text = "termos de uso",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onTermosClick)
        )
    }
}

@Composable
private fun TermosDeUsoDialog(onDispensar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDispensar,
        title = { Text("Termos de uso") },
        text = {
            // Texto provisório: o texto definitivo dos termos está fora do escopo da spec 001
            Text(
                "Ao criar sua conta, você concorda em usar o ExtensionConnect apenas para atividades " +
                    "acadêmicas de extensão da instituição Afya, mantendo seus dados atualizados e " +
                    "respeitando as diretrizes de uso de imagem e informação dos participantes dos projetos."
            )
        },
        confirmButton = {
            TextButton(onClick = onDispensar) { Text("Fechar") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun AcessoScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        AcessoScreen(
            loginUiState = LoginUiState.Erro("E-mail ou senha incorretos."),
            cadastroUiState = CadastroUiState.Ocioso,
            onEntrarClick = { _, _ -> },
            onCadastrarClick = { _, _, _ -> },
            onEsqueceuSenhaClick = {},
            onLoginErroDispensado = {},
            onCadastroErroDispensado = {}
        )
    }
}
