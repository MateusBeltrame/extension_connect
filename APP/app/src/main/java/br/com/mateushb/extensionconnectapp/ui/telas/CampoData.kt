package br.com.mateushb.extensionconnectapp.ui.telas

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.ui.components.AppTextField
import br.com.mateushb.extensionconnectapp.ui.theme.WarningAmber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Formatos de data exibidos nas telas de projeto (lista, quadro e wizard)
private val FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val FORMATO_DIA_MES = DateTimeFormatter.ofPattern("dd/MM")

internal fun LocalDate.formatarData(): String = format(FORMATO_DATA)

internal fun LocalDate.formatarDiaMes(): String = format(FORMATO_DIA_MES)

// Campo somente leitura que abre o calendário do Material 3. O DatePicker trabalha em millis UTC:
// converter nos dois sentidos com ZoneOffset.UTC evita a data "voltar um dia" em fuso negativo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CampoData(
    valor: LocalDate?,
    onValorChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "dd/mm/aaaa",
    erro: String? = null,
    isWarning: Boolean = false,
    enabled: Boolean = true
) {
    var calendarioAberto by rememberSaveable { mutableStateOf(false) }

    AppTextField(
        value = valor?.formatarData().orEmpty(),
        onValueChange = {},
        placeholder = placeholder,
        modifier = modifier,
        label = label,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = if (isWarning) WarningAmber else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        },
        supportingText = erro,
        isError = erro != null,
        isWarning = isWarning,
        onClick = { calendarioAberto = true },
        enabled = enabled
    )

    if (calendarioAberto) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = valor?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        val cores = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        DatePickerDialog(
            onDismissRequest = { calendarioAberto = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let {
                            onValorChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        calendarioAberto = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { calendarioAberto = false }) {
                    Text("Cancelar")
                }
            },
            colors = cores
        ) {
            DatePicker(state = estado, colors = cores)
        }
    }
}
