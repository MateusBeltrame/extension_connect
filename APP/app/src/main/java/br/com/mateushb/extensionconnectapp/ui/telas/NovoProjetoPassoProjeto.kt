package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.ErrosPasso1
import br.com.mateushb.extensionconnectapp.model.ProjetoResumo
import br.com.mateushb.extensionconnectapp.model.RascunhoProjeto
import br.com.mateushb.extensionconnectapp.model.TemaProjeto
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.components.Avatar
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone

// Passo 1 — card "Grupo e projeto" (frame 37:2)
@Composable
internal fun PassoProjeto(
    rascunho: RascunhoProjeto,
    professores: List<Usuario>,
    erros: ErrosPasso1?,
    enabled: Boolean,
    onRascunhoChange: ((RascunhoProjeto) -> RascunhoProjeto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = ESPACO_FIM_PASSO)
    ) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Grupo e projeto",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // take(): o limite impede digitar além do máximo, em vez de cortar depois
                AppTextField(
                    value = rascunho.nomeGrupo,
                    onValueChange = { texto -> onRascunhoChange { it.copy(nomeGrupo = texto.take(RascunhoProjeto.MAX_NOME_GRUPO)) } },
                    placeholder = "Ex.: Raízes",
                    label = "Nome do grupo",
                    supportingText = erros?.nomeGrupo,
                    isError = erros?.nomeGrupo != null,
                    counter = "${rascunho.nomeGrupo.length}/${RascunhoProjeto.MAX_NOME_GRUPO}",
                    enabled = enabled
                )
                AppTextField(
                    value = rascunho.titulo,
                    onValueChange = { texto -> onRascunhoChange { it.copy(titulo = texto.take(RascunhoProjeto.MAX_TITULO)) } },
                    placeholder = "Ex.: Horta comunitária na escola",
                    label = "Título do projeto",
                    supportingText = erros?.titulo,
                    isError = erros?.titulo != null,
                    counter = "${rascunho.titulo.length}/${RascunhoProjeto.MAX_TITULO}",
                    enabled = enabled
                )
                AppTextField(
                    value = rascunho.descricao,
                    onValueChange = { texto -> onRascunhoChange { it.copy(descricao = texto.take(RascunhoProjeto.MAX_DESCRICAO)) } },
                    placeholder = "Conte o objetivo e o público do projeto",
                    label = "Descrição",
                    singleLine = false,
                    height = 110.dp,
                    supportingText = erros?.descricao,
                    isError = erros?.descricao != null,
                    counter = "${rascunho.descricao.length}/${RascunhoProjeto.MAX_DESCRICAO}",
                    enabled = enabled
                )
                CampoTema(
                    tema = rascunho.tema,
                    erro = erros?.tema,
                    enabled = enabled,
                    onTemaChange = { tema -> onRascunhoChange { it.copy(tema = tema) } }
                )
                CampoOrientador(
                    orientador = rascunho.orientador,
                    professores = professores,
                    erro = erros?.orientador,
                    enabled = enabled,
                    onOrientadorChange = { professor -> onRascunhoChange { it.copy(orientador = professor) } }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoData(
                        valor = rascunho.dataInicio,
                        onValorChange = { data -> onRascunhoChange { it.copy(dataInicio = data) } },
                        label = "Data de início",
                        erro = erros?.dataInicio,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )
                    CampoData(
                        valor = rascunho.dataFimPrevisto,
                        onValorChange = { data -> onRascunhoChange { it.copy(dataFimPrevisto = data) } },
                        label = "Fim previsto",
                        erro = erros?.dataFimPrevisto,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoTema(
    tema: TemaProjeto?,
    erro: String?,
    enabled: Boolean,
    onTemaChange: (TemaProjeto) -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }
    Box {
        AppTextField(
            value = tema?.titulo.orEmpty(),
            onValueChange = {},
            placeholder = "Selecione o tema",
            label = "Tema",
            leadingIcon = { IconeCampo(Icons.Outlined.Sell) },
            trailingIcon = { IconeCampo(Icons.Outlined.KeyboardArrowDown) },
            supportingText = erro,
            isError = erro != null,
            onClick = { menuAberto = true },
            enabled = enabled
        )
        DropdownMenu(
            expanded = menuAberto,
            onDismissRequest = { menuAberto = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            TemaProjeto.entries.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(text = opcao.titulo, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onTemaChange(opcao)
                        menuAberto = false
                    }
                )
            }
        }
    }
}

// Busca de professor (mín. 3 letras, nome ou e-mail, sem acento) + cartão roxo do escolhido
@Composable
private fun CampoOrientador(
    orientador: Usuario?,
    professores: List<Usuario>,
    erro: String?,
    enabled: Boolean,
    onOrientadorChange: (Usuario?) -> Unit
) {
    var busca by rememberSaveable { mutableStateOf("") }
    val termo = ProjetoResumo.normalizar(busca)
    val buscaValida = termo.length >= RascunhoProjeto.MIN_LETRAS_BUSCA
    val resultados = if (buscaValida) {
        professores.filter { it.id != orientador?.id && it.correspondeABusca(termo) }
    } else {
        emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "Orientador", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
        AppTextField(
            value = busca,
            onValueChange = { busca = it },
            placeholder = "Buscar professor",
            leadingIcon = { IconeCampo(Icons.Outlined.Search) },
            supportingText = when {
                erro != null -> erro
                busca.isNotBlank() && !buscaValida -> "Digite ao menos 3 letras"
                buscaValida && resultados.isEmpty() -> "Nenhum professor encontrado"
                else -> null
            },
            isError = erro != null,
            enabled = enabled
        )
        if (resultados.isNotEmpty()) {
            AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                resultados.forEachIndexed { indice, professor ->
                    if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOrientadorChange(professor)
                                busca = ""
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Avatar(initials = iniciais(professor.nome), tone = StatusTone.Purple, size = 28.dp)
                        NomeEEmail(professor)
                    }
                }
            }
        }
        if (orientador != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StatusTone.Purple.container, RoundedCornerShape(10.dp))
                    .padding(start = 10.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciais(orientador.nome),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusTone.Purple.onContainer
                    )
                }
                Box(Modifier.weight(1f)) { NomeEEmail(orientador) }
                IconButton(onClick = { onOrientadorChange(null) }, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remover orientador",
                        tint = StatusTone.Purple.onContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Text(
            text = "Exatamente um orientador por projeto",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun NomeEEmail(usuario: Usuario) {
    Column {
        Text(
            text = usuario.nome,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = usuario.email,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Termo já normalizado (ProjetoResumo.normalizar)
internal fun Usuario.correspondeABusca(termo: String): Boolean =
    ProjetoResumo.normalizar(nome).contains(termo) || ProjetoResumo.normalizar(email).contains(termo)
