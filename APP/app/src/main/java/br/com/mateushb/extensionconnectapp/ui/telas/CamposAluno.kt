package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import br.com.mateushb.extensionconnectapp.model.Curso
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField

// RA / Matrícula + Curso (lista fixa), campos só de Aluno em "Editar pessoa" (spec 007) e "Meu perfil"
// (spec 008). Letras são aceitas no RA e apontadas ao salvar (ValidacaoAcesso.erroRa)
@Composable
internal fun CamposAluno(
    ra: String,
    onRaChange: (String) -> Unit,
    raErro: String?,
    curso: Curso?,
    onCursoChange: (Curso) -> Unit,
    enabled: Boolean,
    cursoErro: String? = null
) {
    var cursoExpandido by remember { mutableStateOf(false) }

    AppTextField(
        value = ra,
        onValueChange = onRaChange,
        placeholder = "Ex.: 2023104587",
        label = "RA / Matrícula",
        leadingIcon = { IconeCampo(Icons.Outlined.Tag) },
        supportingText = raErro,
        isError = raErro != null,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
    )
    Box {
        AppTextField(
            value = curso?.titulo.orEmpty(),
            onValueChange = {},
            placeholder = "Selecione o curso",
            label = "Curso",
            leadingIcon = { IconeCampo(Icons.Outlined.School) },
            trailingIcon = { IconeCampo(Icons.Outlined.KeyboardArrowDown) },
            supportingText = cursoErro,
            isError = cursoErro != null,
            enabled = enabled,
            onClick = { cursoExpandido = true }
        )
        DropdownMenu(
            expanded = cursoExpandido,
            onDismissRequest = { cursoExpandido = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Curso.entries.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(text = opcao.titulo, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onCursoChange(opcao)
                        cursoExpandido = false
                    }
                )
            }
        }
    }
}
