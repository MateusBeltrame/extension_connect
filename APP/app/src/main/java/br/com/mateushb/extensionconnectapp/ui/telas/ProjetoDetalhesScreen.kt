package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.model.Projeto
import br.com.mateushb.extensionconnectapp.ui.components.AppTopBar
import br.com.mateushb.extensionconnectapp.ui.components.ErrorRetry
import br.com.mateushb.extensionconnectapp.ui.state.DetalhesProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

// Tela PROVISÓRIA (spec 003, HU-07): o conteúdo real — abas Visão geral/Diário/Arquivos — é da feature 4
@Composable
fun ProjetoDetalhesScreen(
    detalhesUiState: DetalhesProjetoUiState,
    onVoltarClick: () -> Unit,
    onTentarNovamente: () -> Unit
) {
    val titulo = (detalhesUiState as? DetalhesProjetoUiState.Sucesso)?.projeto?.titulo ?: "Projeto"
    Scaffold(
        topBar = {
            AppTopBar(
                title = titulo,
                navigationIcon = {
                    IconButton(onClick = onVoltarClick) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (detalhesUiState) {
                is DetalhesProjetoUiState.Carregando -> CircularProgressIndicator()

                is DetalhesProjetoUiState.Erro -> {
                    ErrorRetry(message = detalhesUiState.mensagem, onRetry = onTentarNovamente)
                }

                is DetalhesProjetoUiState.Sucesso -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HourglassEmpty,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "Em breve",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Os detalhes do projeto chegam na próxima versão.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjetoDetalhesScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        ProjetoDetalhesScreen(
            detalhesUiState = DetalhesProjetoUiState.Sucesso(Projeto(id = 1, titulo = "Horta comunitária na Escola Municipal Vila Nova")),
            onVoltarClick = {},
            onTentarNovamente = {}
        )
    }
}
