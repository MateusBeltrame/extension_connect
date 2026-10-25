package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.components.AppCard
import br.com.mateushb.extensionconnectapp.ui.components.StatusChip
import br.com.mateushb.extensionconnectapp.ui.components.StatusTone
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

// Tela PROVISÓRIA da rota ProjetosLista (spec 001): a feature 2 substitui pelo conteúdo real
@Composable
fun InicioScreen(
    usuario: Usuario,
    onSairClick: () -> Unit
) {
    AcessoLayout {
        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CabecalhoCard(
                    titulo = "Olá, ${usuario.primeiroNome}!",
                    subtitulo = "Seus projetos aparecerão aqui em breve."
                )
                StatusChip(label = usuario.perfil.titulo, tone = tomDoPerfil(usuario.perfil))
                usuario.curso?.let { curso ->
                    Text(
                        text = "${curso.titulo} · RA ${usuario.ra}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onSairClick,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Sair da conta")
                }
            }
        }
    }
}

private fun tomDoPerfil(perfil: PerfilUsuario): StatusTone = when (perfil) {
    PerfilUsuario.ADMINISTRADOR -> StatusTone.Purple
    PerfilUsuario.PROFESSOR -> StatusTone.Blue
    PerfilUsuario.ALUNO -> StatusTone.Pink
}

@Preview(showBackground = true)
@Composable
private fun InicioScreenPreview() {
    ExtensionConnectAPPTheme(darkTheme = false) {
        InicioScreen(usuario = Usuario(id = 1, nome = "Administrador", perfil = PerfilUsuario.ADMINISTRADOR), onSairClick = {})
    }
}
