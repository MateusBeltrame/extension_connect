package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.state.CompletarCadastroUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

// Gate (spec 001, HU-04): sem seta de voltar; a única saída além de salvar é "Sair da conta"
@Composable
fun CompletarCadastroScreen(
    usuario: Usuario,
    completarCadastroUiState: CompletarCadastroUiState,
    onSalvarClick: (ra: String, curso: Curso) -> Unit,
    onSairClick: () -> Unit,
    onErroMostrado: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(completarCadastroUiState) {
        if (completarCadastroUiState is CompletarCadastroUiState.Erro) {
            snackbarHostState.showSnackbar(completarCadastroUiState.mensagem)
            onErroMostrado()
        }
    }

    AcessoLayout(snackbarHostState = snackbarHostState, horizontalAlignment = Alignment.CenterHorizontally) {
        FormularioCompletarCadastro(
            usuario = usuario,
            salvando = completarCadastroUiState is CompletarCadastroUiState.Salvando,
            onSalvarClick = onSalvarClick
        )
        Row(
            modifier = Modifier
                .clickable(onClick = onSairClick)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Sair da conta",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormularioCompletarCadastro(
    usuario: Usuario,
    salvando: Boolean,
    onSalvarClick: (String, Curso) -> Unit
) {
    var ra by rememberSaveable(usuario.id) { mutableStateOf(usuario.ra ?: "") }
    var curso by rememberSaveable(usuario.id) { mutableStateOf(usuario.curso) }
    var dropdownExpandido by remember { mutableStateOf(false) }
    var raErro by remember { mutableStateOf<String?>(null) }
    var cursoErro by remember { mutableStateOf<String?>(null) }

    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            CabecalhoCard(
                titulo = "Falta pouco, ${usuario.primeiroNome}!",
                subtitulo = "Para usar o app, informe seu RA e seu curso. Isso só é pedido uma vez."
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppTextField(
                    value = ra,
                    // RA aceita apenas dígitos
                    onValueChange = { novo -> ra = novo.filter { it in '0'..'9' }; raErro = null },
                    placeholder = "Ex.: 2023104587",
                    label = "RA / Matrícula",
                    leadingIcon = { IconeCampo(Icons.Outlined.Tag) },
                    supportingText = raErro ?: "Apenas números",
                    isError = raErro != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                Box {
                    AppTextField(
                        value = curso?.titulo ?: "",
                        onValueChange = {},
                        placeholder = "Selecione seu curso",
                        label = "Curso",
                        leadingIcon = { IconeCampo(Icons.Outlined.School) },
                        trailingIcon = { IconeCampo(Icons.Outlined.KeyboardArrowDown) },
                        supportingText = cursoErro,
                        isError = cursoErro != null,
                        onClick = { if (!salvando) dropdownExpandido = true }
                    )
                    DropdownMenu(
                        expanded = dropdownExpandido,
                        onDismissRequest = { dropdownExpandido = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Curso.entries.forEach { opcao ->
                            DropdownMenuItem(
                                text = { Text(text = opcao.titulo, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    curso = opcao
                                    cursoErro = null
                                    dropdownExpandido = false
                                }
                            )
                        }
                    }
                }
            }
            PrimaryButton(
                text = "Salvar e continuar",
                loading = salvando,
                onClick = {
                    raErro = ValidacaoAcesso.erroRa(ra, obrigatorio = true)
                    cursoErro = if (curso == null) "Selecione o curso" else null
                    val cursoEscolhido = curso
                    if (raErro == null && cursoEscolhido != null) onSalvarClick(ra, cursoEscolhido)
                }
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun CompletarCadastroScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        CompletarCadastroScreen(
            usuario = Usuario(id = 1, nome = "Mateus Henrique Silva"),
            completarCadastroUiState = CompletarCadastroUiState.Ocioso,
            onSalvarClick = { _, _ -> },
            onSairClick = {},
            onErroMostrado = {}
        )
    }
}
