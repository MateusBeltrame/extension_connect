package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.model.TemaProjeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppTopBar
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.components.PrimaryButton
import br.com.mateushb.extensionconnectapp.ui.components.SecondaryButton
import br.com.mateushb.extensionconnectapp.ui.state.FormularioProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

private val PASSOS = listOf("Projeto", "Cronograma", "Participantes")

// Wizard de 3 passos (spec 003, frames 37:2/37:109/37:267/37:393). O rascunho e o passo atual vêm do
// NovoProjetoViewModel; a tela guarda só o efêmero (quais erros mostrar, diálogo, Snackbar)
@Composable
fun NovoProjetoScreen(
    formularioUiState: FormularioProjetoUiState,
    salvarUiState: SalvarProjetoUiState,
    rascunho: RascunhoProjeto,
    passoAtual: Int,
    orientadorInicial: Usuario?,
    onRascunhoChange: ((RascunhoProjeto) -> RascunhoProjeto) -> Unit,
    onAvancarClick: () -> Unit,
    onVoltarClick: () -> Unit,
    onAdicionarEtapa: (String) -> Unit,
    onRemoverEtapa: (Long) -> Unit,
    onMoverEtapa: (de: Int, para: Int) -> Unit,
    onAdicionarParticipante: (Usuario) -> Unit,
    onRemoverParticipante: (Int) -> Unit,
    onSalvarClick: () -> Unit,
    onFecharClick: () -> Unit,
    onTentarNovamente: () -> Unit,
    onErroMostrado: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val salvando = salvarUiState is SalvarProjetoUiState.Salvando

    // Erros só aparecem depois da primeira tentativa de avançar/salvar em cada passo
    var tentouPasso1 by rememberSaveable { mutableStateOf(false) }
    var tentouPasso2 by rememberSaveable { mutableStateOf(false) }
    var tentouPasso3 by rememberSaveable { mutableStateOf(false) }
    var mostrarDescartar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(salvarUiState) {
        if (salvarUiState is SalvarProjetoUiState.Erro) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(salvarUiState.mensagem)
            onErroMostrado()
        }
    }

    // X e o voltar do sistema no Passo 1: pergunta antes de perder o que foi preenchido (HU-06)
    val fechar: () -> Unit = {
        if (rascunho.preenchido(orientadorInicial)) mostrarDescartar = true else onFecharClick()
    }
    val voltar: () -> Unit = { if (passoAtual > 1) onVoltarClick() else fechar() }

    // Durante o salvamento o voltar é consumido e ignorado
    BackHandler { if (!salvando) voltar() }

    Scaffold(
        topBar = {
            Column {
                AppTopBar(
                    title = "Novo projeto",
                    navigationIcon = {
                        IconButton(onClick = { if (!salvando) fechar() }) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                )
                IndicadorPassos(passoAtual = passoAtual)
            }
        },
        bottomBar = {
            if (formularioUiState is FormularioProjetoUiState.Sucesso) {
                RodapeWizard(
                    passoAtual = passoAtual,
                    salvando = salvando,
                    onVoltarClick = onVoltarClick,
                    onAvancarClick = {
                        when (passoAtual) {
                            1 -> {
                                tentouPasso1 = true
                                if (rascunho.errosPasso1().valido) onAvancarClick()
                            }

                            2 -> {
                                tentouPasso2 = true
                                if (rascunho.errosPasso2().valido) onAvancarClick()
                            }

                            else -> {
                                tentouPasso3 = true
                                if (rascunho.errosPasso3() == null) onSalvarClick()
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // O rodapé já ocupa a parte de baixo: só o que o teclado passar dele vira padding
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            when (formularioUiState) {
                is FormularioProjetoUiState.Carregando -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is FormularioProjetoUiState.Erro -> {
                    ErrorRetry(
                        message = formularioUiState.mensagem,
                        onRetry = onTentarNovamente,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is FormularioProjetoUiState.Sucesso -> when (passoAtual) {
                    1 -> PassoProjeto(
                        rascunho = rascunho,
                        professores = formularioUiState.professores,
                        erros = if (tentouPasso1) rascunho.errosPasso1() else null,
                        enabled = !salvando,
                        onRascunhoChange = onRascunhoChange
                    )

                    2 -> PassoCronograma(
                        rascunho = rascunho,
                        enabled = !salvando,
                        erros = if (tentouPasso2) rascunho.errosPasso2() else null,
                        onRascunhoChange = onRascunhoChange,
                        onAdicionarEtapa = onAdicionarEtapa,
                        onRemoverEtapa = onRemoverEtapa,
                        onMoverEtapa = onMoverEtapa
                    )

                    else -> PassoParticipantes(
                        rascunho = rascunho,
                        candidatos = formularioUiState.candidatos,
                        erro = if (tentouPasso3) rascunho.errosPasso3() else null,
                        enabled = !salvando,
                        onAdicionarParticipante = onAdicionarParticipante,
                        onRemoverParticipante = onRemoverParticipante
                    )
                }
            }
        }
    }

    if (mostrarDescartar) {
        AlertDialog(
            onDismissRequest = { mostrarDescartar = false },
            title = { Text("Descartar projeto?") },
            text = { Text("Os dados preenchidos serão perdidos.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDescartar = false
                        onFecharClick()
                    }
                ) {
                    Text("Descartar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDescartar = false }) {
                    Text("Continuar editando")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Faixa branca "1 Projeto — 2 Cronograma — 3 Participantes": passo concluído vira check rosa e a linha
// até o próximo também fica rosa
@Composable
private fun IndicadorPassos(passoAtual: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PASSOS.forEachIndexed { indice, nome ->
                val numero = indice + 1
                if (indice > 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (numero <= passoAtual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
                ItemPasso(numero = numero, nome = nome, passoAtual = passoAtual)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ItemPasso(numero: Int, nome: String, passoAtual: Int) {
    val concluido = numero < passoAtual
    val ativo = numero == passoAtual
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .then(
                    if (concluido || ativo) {
                        Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (concluido) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Concluído",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(13.dp)
                )
            } else {
                Text(
                    text = numero.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ativo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                )
            }
        }
        Text(
            text = nome,
            fontSize = 12.sp,
            fontWeight = if (ativo) FontWeight.Bold else FontWeight.Medium,
            color = if (concluido || ativo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            maxLines = 1
        )
    }
}

// Passo 1 só tem "Avançar"; os demais têm "Voltar" + "Avançar"/"Salvar projeto"
@Composable
private fun RodapeWizard(
    passoAtual: Int,
    salvando: Boolean,
    onVoltarClick: () -> Unit,
    onAvancarClick: () -> Unit
) {
    Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (passoAtual > 1) {
                SecondaryButton(
                    text = "Voltar",
                    onClick = onVoltarClick,
                    enabled = !salvando,
                    leadingIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                    modifier = Modifier.weight(1f)
                )
            }
            if (passoAtual < 3) {
                PrimaryButton(
                    text = "Avançar",
                    onClick = onAvancarClick,
                    trailingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryButton(
                    text = "Salvar projeto",
                    onClick = onAvancarClick,
                    loading = salvando,
                    leadingIcon = Icons.Outlined.Check,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NovoProjetoScreenPreview() {
    val professora = Usuario(id = 1, nome = "Ana Ribeiro", email = "ana.ribeiro@afya.br", perfil = PerfilUsuario.PROFESSOR)
    ExtensionConnectAPPTheme(darkTheme = false) {
        NovoProjetoScreen(
            formularioUiState = FormularioProjetoUiState.Sucesso(professores = listOf(professora), candidatos = emptyList()),
            salvarUiState = SalvarProjetoUiState.Ocioso,
            rascunho = RascunhoProjeto(
                nomeGrupo = "Grupo Raízes",
                titulo = "Horta comunitária na Escola Municipal Vila Nova",
                tema = TemaProjeto.MEIO_AMBIENTE,
                orientador = professora
            ),
            passoAtual = 1,
            orientadorInicial = professora,
            onRascunhoChange = {}, onAvancarClick = {}, onVoltarClick = {}, onAdicionarEtapa = {},
            onRemoverEtapa = {}, onMoverEtapa = { _, _ -> }, onAdicionarParticipante = {},
            onRemoverParticipante = {}, onSalvarClick = {}, onFecharClick = {}, onTentarNovamente = {},
            onErroMostrado = {}
        )
    }
}

// Espaço entre o fim do conteúdo rolável e o rodapé fixo
internal val ESPACO_FIM_PASSO = 20.dp

@Composable
internal fun TituloPasso(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = titulo, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(text = subtitulo, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
