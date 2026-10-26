package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.model.FiltroPessoas
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.AppTopBar
import br.com.mateushb.extensionconnectapp.ui.components.Avatar
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.state.ListaPessoasUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme
import kotlinx.coroutines.launch

// Chips do frame 36:2, na ordem do Figma; null = "Todos"
private val CHIPS_PERFIL: List<Pair<String, PerfilUsuario?>> = listOf(
    "Todos" to null,
    "Admin" to PerfilUsuario.ADMINISTRADOR,
    "Professor" to PerfilUsuario.PROFESSOR,
    "Aluno" to PerfilUsuario.ALUNO
)

// Tela Pessoas, só Administrador (spec 007, frame 36:2). mensagem: Snackbar de quem voltou de
// "Editar pessoa" depois de salvar
@Composable
fun PessoasListaScreen(
    listaUiState: ListaPessoasUiState,
    busca: String,
    filtroAtual: FiltroPessoas,
    mensagem: String?,
    onBuscaChange: (String) -> Unit,
    onFiltroChange: (FiltroPessoas) -> Unit,
    onPessoaClick: (usuarioId: Int) -> Unit,
    onTentarNovamente: () -> Unit,
    onMensagemMostrada: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    // A mensagem é consumida na hora; o Snackbar roda no escopo da tela para não ser cancelado
    // quando a chave do LaunchedEffect volta a null
    LaunchedEffect(mensagem) {
        if (mensagem != null) {
            escopo.launch { snackbarHostState.showSnackbar(mensagem) }
            onMensagemMostrada()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Pessoas",
                subtitle = (listaUiState as? ListaPessoasUiState.Sucesso)?.let { textoTotal(it.total) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        // Busca, filtros e lista rolam juntos (fixos, em paisagem sobrariam ~60dp para a lista). LazyColumn:
        // a busca refiltra a cada letra e só as linhas visíveis são compostas
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item(key = "filtros") {
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppTextField(
                        value = busca,
                        onValueChange = onBuscaChange,
                        placeholder = "Buscar por nome ou e-mail",
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CHIPS_PERFIL.forEach { (rotulo, perfil) ->
                            ChipFiltro(
                                label = rotulo,
                                selecionado = filtroAtual.perfil == perfil,
                                onClick = { onFiltroChange(filtroAtual.copy(perfil = perfil)) },
                                tamanhoTexto = 13.sp,
                                mostrarCheck = false
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Mostrar inativos",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        SwitchMarca(
                            checked = filtroAtual.mostrarInativos,
                            onCheckedChange = { onFiltroChange(filtroAtual.copy(mostrarInativos = it)) }
                        )
                    }
                }
            }

            // Dentro da rolagem não há altura para preencher: os estados ocupam uma faixa fixa
            val estadoModifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp)
            when (listaUiState) {
                is ListaPessoasUiState.Carregando -> item(key = "carregando") {
                    Box(estadoModifier, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ListaPessoasUiState.Erro -> item(key = "erro") {
                    ErrorRetry(message = listaUiState.mensagem, onRetry = onTentarNovamente, modifier = estadoModifier)
                }

                is ListaPessoasUiState.Sucesso -> {
                    val pessoas = listaUiState.pessoas
                    if (pessoas.isEmpty()) {
                        item(key = "vazio") {
                            Box(estadoModifier, contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Nenhuma pessoa encontrada",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        itemsIndexed(pessoas, key = { _, pessoa -> pessoa.id }) { indice, pessoa ->
                            TrechoDoCard(primeiro = indice == 0, ultimo = indice == pessoas.lastIndex) {
                                if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                PessoaLinha(pessoa = pessoa, onClick = { onPessoaClick(pessoa.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// Um único card com divisórias (frame 36:2), montado item a item na LazyColumn: só o primeiro e o
// último têm cantos e respiro de 4dp. O fundo do card (com a sombra) é desenhado mais comprido que o
// item nas emendas e recortado na altura do item: a sombra lateral fica contínua, sem afinar nem marcar
// linhas entre os itens, e só aparece nas pontas de cima e de baixo do card
@Composable
private fun TrechoDoCard(primeiro: Boolean, ultimo: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val canto = 12.dp
    val forma = RoundedCornerShape(
        topStart = if (primeiro) canto else 0.dp,
        topEnd = if (primeiro) canto else 0.dp,
        bottomStart = if (ultimo) canto else 0.dp,
        bottomEnd = if (ultimo) canto else 0.dp
    )
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .drawWithContent {
                val folga = 16.dp.toPx()
                clipRect(
                    left = -folga,
                    top = if (primeiro) -folga else 0f,
                    right = size.width + folga,
                    bottom = if (ultimo) size.height + folga else size.height
                ) { this@drawWithContent.drawContent() }
            }
    ) {
        AppCard(
            modifier = Modifier
                .matchParentSize()
                .layout { medida, restricoes ->
                    val acimaPx = if (primeiro) 0 else 16.dp.roundToPx()
                    val abaixoPx = if (ultimo) 0 else 16.dp.roundToPx()
                    val fundo = medida.measure(
                        Constraints.fixed(restricoes.maxWidth, restricoes.maxHeight + acimaPx + abaixoPx)
                    )
                    layout(restricoes.maxWidth, restricoes.maxHeight) { fundo.place(0, -acimaPx) }
                },
            contentPadding = 0.dp,
            shape = forma
        ) {}
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = if (primeiro) 4.dp else 0.dp, bottom = if (ultimo) 4.dp else 0.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PessoaLinha(pessoa: Usuario, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvatarPessoa(pessoa = pessoa, tamanho = 40)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = pessoa.nome,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = if (pessoa.ativo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pessoa.email,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusChip(label = pessoa.perfil.titulo, tone = tomDoPerfil(pessoa.perfil))
                if (pessoa.cadastroIncompleto) {
                    ChipComIcone("Cadastro incompleto", StatusTone.Orange, Icons.Outlined.ErrorOutline)
                }
                if (!pessoa.ativo) {
                    ChipComIcone("Inativo", StatusTone.Neutral, Icons.Outlined.PersonOff)
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Editar ${pessoa.nome}",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ===== Apresentação compartilhada com Editar pessoa =====

// Cores do avatar e do selo por perfil (frames 36:2 e 36:175)
internal fun tomDoPerfil(perfil: PerfilUsuario): StatusTone = when (perfil) {
    PerfilUsuario.ADMINISTRADOR -> StatusTone.Pink
    PerfilUsuario.PROFESSOR -> StatusTone.Purple
    PerfilUsuario.ALUNO -> StatusTone.Blue
}

// Pessoa inativa: avatar cinza com iniciais claras (Carla Mendes, frame 36:2)
@Composable
internal fun AvatarPessoa(pessoa: Usuario, tamanho: Int) {
    if (pessoa.ativo) {
        Avatar(initials = iniciais(pessoa.nome), tone = tomDoPerfil(pessoa.perfil), size = tamanho.dp)
    } else {
        Avatar(
            initials = iniciais(pessoa.nome),
            tone = StatusTone.Neutral,
            size = tamanho.dp,
            contentColor = MaterialTheme.colorScheme.outline
        )
    }
}

// Switch nas cores da marca; desabilitado e ligado fica rosa claro (frame 36:420)
@Composable
internal fun SwitchMarca(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            disabledCheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
            disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledCheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f)
        )
    )
}

private fun textoTotal(total: Int): String =
    if (total == 1) "1 pessoa cadastrada" else "$total pessoas cadastradas"

// ===== Previews =====

@Preview(showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun PessoasListaScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        PessoasListaScreen(
            listaUiState = ListaPessoasUiState.Sucesso(
                pessoas = listOf(
                    Usuario(id = 1, nome = "Ana Ribeiro", email = "ana.ribeiro@afya.br", perfil = PerfilUsuario.PROFESSOR),
                    Usuario(id = 2, nome = "Bianca Nunes", email = "bianca.nunes@afya.br", ra = "1", curso = Curso.DIREITO),
                    Usuario(id = 3, nome = "Bruno Costa", email = "bruno.costa@afya.br"),
                    Usuario(id = 4, nome = "Carla Mendes", email = "carla.mendes@afya.br", ativo = false, ra = "2", curso = Curso.MEDICINA),
                    Usuario(id = 5, nome = "Renata Alves", email = "renata.alves@afya.br", perfil = PerfilUsuario.ADMINISTRADOR)
                ),
                total = 48
            ),
            busca = "",
            filtroAtual = FiltroPessoas(mostrarInativos = true),
            mensagem = null,
            onBuscaChange = {},
            onFiltroChange = {},
            onPessoaClick = {},
            onTentarNovamente = {},
            onMensagemMostrada = {}
        )
    }
}
