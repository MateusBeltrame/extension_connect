package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.ArquivoDetalhes
import br.com.mateushb.extensionconnectapp.model.EnvioPendente
import br.com.mateushb.extensionconnectapp.model.FormatoDocumento
import br.com.mateushb.extensionconnectapp.model.FotoDetalhes
import br.com.mateushb.extensionconnectapp.model.StatusEnvio
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.components.MiniaturaFoto
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.state.ListaArquivosUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ErrorContainerLight
import br.com.mateushb.extensionconnectapp.ui.theme.ErrorRed
import br.com.mateushb.extensionconnectapp.ui.theme.InfoBlue
import br.com.mateushb.extensionconnectapp.ui.theme.InfoContainerLight
import br.com.mateushb.extensionconnectapp.ui.theme.SuccessContainerLight
import br.com.mateushb.extensionconnectapp.ui.theme.SuccessGreenStrong
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber
import java.time.LocalDate

private const val COLUNAS_FOTOS = 3

// Aba Arquivos dos detalhes do projeto (spec 006, frame 35:231): cards Fotos, Documentos e, se o usuário
// tiver envios pendentes, Envios em andamento. Entra como itens da LazyColumn da tela, como o Diário
internal fun LazyListScope.abaArquivos(
    listaUiState: ListaArquivosUiState,
    usuarioId: Int,
    hoje: LocalDate,
    podeExcluir: (autorId: Int) -> Boolean,
    onFotoClick: (FotoDetalhes) -> Unit,
    onAbrirDocumento: (ArquivoDetalhes) -> Unit,
    onExcluirDocumento: (ArquivoDetalhes) -> Unit,
    onTentarNovamente: () -> Unit
) {
    when (listaUiState) {
        is ListaArquivosUiState.Carregando -> item(key = "arquivos-carregando") {
            Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ListaArquivosUiState.Erro -> item(key = "arquivos-erro") {
            Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                ErrorRetry(message = listaUiState.mensagem, onRetry = onTentarNovamente)
            }
        }

        is ListaArquivosUiState.Sucesso -> {
            val arquivos = listaUiState.arquivos
            item(key = "arquivos-fotos") {
                CardFotos(fotos = arquivos.fotosEnviadas, onFotoClick = onFotoClick)
            }
            item(key = "arquivos-documentos") {
                CardDocumentos(
                    documentos = arquivos.documentosEnviados,
                    hoje = hoje,
                    podeExcluir = podeExcluir,
                    onAbrir = onAbrirDocumento,
                    onExcluir = onExcluirDocumento
                )
            }
            val pendentes = arquivos.enviosPendentes(usuarioId)
            if (pendentes.isNotEmpty()) {
                item(key = "arquivos-envios") { CardEnviosEmAndamento(pendentes) }
            }
        }
    }
}

// ===== Fotos =====

@Composable
private fun CardFotos(fotos: List<FotoDetalhes>, onFotoClick: (FotoDetalhes) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TituloCard("Fotos")
                StatusChip(label = fotos.size.toString())
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "= autorizada no Mural",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (fotos.isEmpty()) {
                TextoVazio("Nenhuma foto enviada")
            } else {
                // Grade de 3 colunas dentro do card (a aba já está numa LazyColumn)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    fotos.chunked(COLUNAS_FOTOS).forEach { linha ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            linha.forEach { detalhes ->
                                FotoDaGrade(
                                    detalhes = detalhes,
                                    onClick = { onFotoClick(detalhes) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(COLUNAS_FOTOS - linha.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

// Quadrado com a foto e, se autorizada no Mural, o globo rosa num círculo branco no canto
@Composable
private fun FotoDaGrade(detalhes: FotoDetalhes, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val foto = detalhes.foto
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClickLabel = "Ver foto", onClick = onClick)
    ) {
        MiniaturaFoto(caminho = foto.caminhoLocal, semente = foto.id, modifier = Modifier.matchParentSize())
        if (foto.autorizadaMural) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Public,
                    contentDescription = "Autorizada no Mural",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

// ===== Documentos =====

@Composable
private fun CardDocumentos(
    documentos: List<ArquivoDetalhes>,
    hoje: LocalDate,
    podeExcluir: (autorId: Int) -> Boolean,
    onAbrir: (ArquivoDetalhes) -> Unit,
    onExcluir: (ArquivoDetalhes) -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TituloCard("Documentos")
                StatusChip(label = documentos.size.toString())
            }
            if (documentos.isEmpty()) {
                TextoVazio("Nenhum documento enviado")
            } else {
                documentos.forEach { detalhes ->
                    LinhaDocumento(
                        detalhes = detalhes,
                        hoje = hoje,
                        podeExcluir = podeExcluir(detalhes.arquivo.autorId),
                        onAbrir = { onAbrir(detalhes) },
                        onExcluir = { onExcluir(detalhes) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LinhaDocumento(
    detalhes: ArquivoDetalhes,
    hoje: LocalDate,
    podeExcluir: Boolean,
    onAbrir: () -> Unit,
    onExcluir: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SeloDocumento(formato = detalhes.arquivo.extensao, mostrarExtensao = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = detalhes.arquivo.nomeArquivo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detalhes.legendaExibida(hoje),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        MenuDocumento(podeExcluir = podeExcluir, onAbrir = onAbrir, onExcluir = onExcluir)
    }
}

// ⋮ do documento (18dp no Figma; área de toque de 32dp): Abrir para todos, Excluir só para o autor
@Composable
private fun MenuDocumento(podeExcluir: Boolean, onAbrir: () -> Unit, onExcluir: () -> Unit) {
    var menuAberto by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menuAberto = true }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Opções do documento",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = menuAberto,
            onDismissRequest = { menuAberto = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DropdownMenuItem(
                text = { Text("Abrir", style = MaterialTheme.typography.bodyLarge) },
                leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
                onClick = {
                    menuAberto = false
                    onAbrir()
                }
            )
            if (podeExcluir) {
                DropdownMenuItem(
                    text = { Text("Excluir documento", style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    onClick = {
                        menuAberto = false
                        onExcluir()
                    }
                )
            }
        }
    }
}

// Selo de 40dp com ícone de arquivo e a extensão nas cores do formato (frame 35:231). Na linha de
// envio pendente o Figma mostra só o ícone, maior
@Composable
private fun SeloDocumento(formato: FormatoDocumento, mostrarExtensao: Boolean) {
    val (fundo, cor) = coresDoFormato(formato)
    Column(
        modifier = Modifier
            .size(40.dp)
            .background(fundo, RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = formato.name,
            tint = cor,
            modifier = Modifier.size(if (mostrarExtensao) 16.dp else 18.dp)
        )
        if (mostrarExtensao) {
            Text(
                text = formato.name,
                color = cor,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                lineHeight = 10.sp
            )
        }
    }
}

private fun coresDoFormato(formato: FormatoDocumento): Pair<Color, Color> = when (formato) {
    FormatoDocumento.PDF -> ErrorContainerLight to ErrorRed
    FormatoDocumento.DOCX -> InfoContainerLight to InfoBlue
    FormatoDocumento.XLSX -> SuccessContainerLight to SuccessGreenStrong
    FormatoDocumento.TXT -> StatusTone.Neutral.container to StatusTone.Neutral.onContainer
}

// ===== Envios em andamento =====

@Composable
private fun CardEnviosEmAndamento(pendentes: List<EnvioPendente>) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TituloCard("Envios em andamento")
            pendentes.forEach { LinhaEnvio(it) }
        }
    }
}

@Composable
private fun LinhaEnvio(envio: EnvioPendente) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val foto = envio.foto
        if (foto != null) {
            MiniaturaFoto(
                caminho = foto.caminhoLocal,
                semente = foto.id,
                modifier = Modifier.size(40.dp),
                tamanhoIcone = 0.dp,
                ladoMaximoPx = 120
            )
        } else {
            SeloDocumento(formato = envio.formato ?: FormatoDocumento.PDF, mostrarExtensao = false)
        }

        if (envio.statusEnvio == StatusEnvio.ENVIANDO) {
            val progresso = (envio.progresso ?: 0).coerceIn(0, 100)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NomeEnvio(envio.nomeArquivo, Modifier.weight(1f))
                    Text(
                        text = "Enviando $progresso%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { progresso / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        } else {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                NomeEnvio(envio.nomeArquivo)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.WifiOff,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Aguardando envio · sem internet",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Medium,
                        color = WarningAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun NomeEnvio(nome: String, modifier: Modifier = Modifier) {
    Text(
        text = nome,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun TextoVazio(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline
    )
}

// ===== Exclusão =====

@Composable
internal fun ConfirmacaoExclusaoArquivoDialog(texto: String, onConfirmar: () -> Unit, onDispensar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDispensar,
        title = { Text("Confirmar exclusão") },
        text = { Text(texto) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDispensar) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
