package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTopBar
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.state.TrocarSenhaUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import kotlinx.coroutines.launch

// Trocar senha (spec 008, frames 36:698 e 36:754). "Senha atual incorreta" aparece no próprio campo
@Composable
fun TrocarSenhaScreen(
    trocarSenhaUiState: TrocarSenhaUiState,
    onSalvarClick: (senhaAtual: String, novaSenha: String) -> Unit,
    onVoltarClick: () -> Unit,
    onSalvoComSucesso: () -> Unit,
    onErroMostrado: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    // Senhas em remember (não rememberSaveable): não podem ir em texto puro para o savedInstanceState, como
    // nas telas de acesso. Erros e visibilidade sobrevivem à rotação
    var senhaAtual by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var senhaAtualErro by rememberSaveable { mutableStateOf<String?>(null) }
    var novaSenhaErro by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmacaoErro by rememberSaveable { mutableStateOf<String?>(null) }
    var atualVisivel by rememberSaveable { mutableStateOf(false) }
    var novaVisivel by rememberSaveable { mutableStateOf(false) }
    var confirmacaoVisivel by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(trocarSenhaUiState) {
        when (trocarSenhaUiState) {
            is TrocarSenhaUiState.Sucesso -> onSalvoComSucesso()
            is TrocarSenhaUiState.SenhaAtualIncorreta -> {
                senhaAtualErro = "Senha atual incorreta"
                onErroMostrado()
            }
            is TrocarSenhaUiState.Erro -> {
                // Snackbar no escopo da tela: limpar o estado reinicia este LaunchedEffect
                escopo.launch { snackbarHostState.showSnackbar(trocarSenhaUiState.mensagem) }
                onErroMostrado()
            }
            else -> Unit
        }
    }

    val salvando = trocarSenhaUiState is TrocarSenhaUiState.Salvando
    val minimo = ValidacaoAcesso.TAMANHO_MINIMO_SENHA

    // Enquanto salva não se sai da tela: o resultado (senha trocada ou não) sempre chega ao usuário
    BackHandler(enabled = salvando) {}

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Trocar senha",
                navigationIcon = {
                    IconButton(onClick = onVoltarClick, enabled = !salvando) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(
                        text = "Por segurança, confirme sua senha atual antes de criar uma nova.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CampoSenha(
                        valor = senhaAtual,
                        onValorChange = {
                            senhaAtual = it
                            senhaAtualErro = null
                            if (novaSenhaErro == ERRO_SENHA_IGUAL) novaSenhaErro = null
                        },
                        rotulo = "Senha atual",
                        visivel = atualVisivel,
                        onAlternarVisibilidade = { atualVisivel = !atualVisivel },
                        erro = senhaAtualErro,
                        imeAction = ImeAction.Next,
                        destacarErro = true,
                        enabled = !salvando
                    )
                    CampoSenha(
                        valor = novaSenha,
                        onValorChange = {
                            novaSenha = it
                            novaSenhaErro = null
                            confirmacaoErro = null
                        },
                        rotulo = "Nova senha",
                        visivel = novaVisivel,
                        onAlternarVisibilidade = { novaVisivel = !novaVisivel },
                        erro = novaSenhaErro,
                        imeAction = ImeAction.Next,
                        ajuda = "Mínimo de $minimo caracteres",
                        enabled = !salvando
                    )
                    CampoSenha(
                        valor = confirmacao,
                        onValorChange = { confirmacao = it; confirmacaoErro = null },
                        rotulo = "Confirmar nova senha",
                        visivel = confirmacaoVisivel,
                        onAlternarVisibilidade = { confirmacaoVisivel = !confirmacaoVisivel },
                        erro = confirmacaoErro,
                        imeAction = ImeAction.Done,
                        confirmado = novaSenha.length >= minimo && confirmacao == novaSenha,
                        enabled = !salvando
                    )
                    PrimaryButton(
                        text = "Salvar nova senha",
                        loading = salvando,
                        onClick = {
                            senhaAtualErro = if (senhaAtual.isEmpty()) "Informe a senha atual" else null
                            novaSenhaErro = when {
                                novaSenha.length < minimo -> "A senha deve ter no mínimo $minimo caracteres"
                                novaSenha == senhaAtual -> ERRO_SENHA_IGUAL
                                else -> null
                            }
                            confirmacaoErro = if (confirmacao != novaSenha) "As senhas não conferem" else null
                            if (senhaAtualErro == null && novaSenhaErro == null && confirmacaoErro == null) {
                                onSalvarClick(senhaAtual, novaSenha)
                            }
                        }
                    )
                }
            }
        }
    }
}

private const val ERRO_SENHA_IGUAL = "A nova senha deve ser diferente da atual"

// ===== Previews =====

@Preview(showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun TrocarSenhaScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        TrocarSenhaScreen(
            trocarSenhaUiState = TrocarSenhaUiState.Ocioso,
            onSalvarClick = { _, _ -> },
            onVoltarClick = {},
            onSalvoComSucesso = {},
            onErroMostrado = {}
        )
    }
}
