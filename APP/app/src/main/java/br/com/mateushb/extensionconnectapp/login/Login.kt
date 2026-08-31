package br.com.mateushb.extensionconnectapp.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mateushb.extensionconnectapp.ui.theme.ExtensionConnectAPPTheme

private val Pink = Color(0xFFE50078)
private val HeaderPink = Color(0xFFE9007A)
private val Ink = Color(0xFF17131A)
private val ScreenBackground = Color(0xFFF8F7FB)
private val FieldBorder = Color(0xFFE4E2E6)
private val Muted = Color(0xFF6F6A73)

@Composable
fun Login() {
    var showingRegistration by remember { mutableStateOf(false) }
    Box(Modifier
        .fillMaxSize()
        .background(ScreenBackground)) {
        Column(Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())) {
            HeaderLogin()
            Spacer(Modifier.height(18.dp))
            AuthCard(
                showingRegistration,
                { showingRegistration = false },
                { showingRegistration = true })
        }
    }
}

@Composable
private fun HeaderLogin() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
            .background(Brush.verticalGradient(listOf(HeaderPink, Color(0xFF230C1A), Ink)))
            .padding(top = 38.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("🎓", fontSize = 22.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "ExtensionConnect",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text("GESTÃO ACADÊMICA DE EXTENSÃO", color = Color(0xFFE2DCE1), fontSize = 10.sp)
    }
}

@Composable
private fun AuthCard(
    isRegistration: Boolean,
    onSelectLogin: () -> Unit,
    onSelectRegistration: () -> Unit
) {
    Column(
        Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        AuthTabs(isRegistration, onSelectLogin, onSelectRegistration)
        Spacer(Modifier.height(23.dp))
        if (isRegistration) RegistrationForm(onSelectLogin)
        else LoginForm(onSelectRegistration)
    }
}

@Composable
private fun AuthTabs(
    isRegistration: Boolean,
    onSelectLogin: () -> Unit,
    onSelectRegistration: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFFF9F9FB)), verticalAlignment = Alignment.CenterVertically
    ) {
        AuthTab("Entrar", !isRegistration, onSelectLogin, Modifier.weight(1f))
        AuthTab("Cadastrar-se", isRegistration, onSelectRegistration, Modifier.weight(1f))
    }
}

@Composable
private fun AuthTab(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Pink else Color(0xFF4D4851),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LoginForm(onCreateAccount: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    FormTitle("Bem-vindo de volta", "Identifique-se para acessar seus projetos")
    Spacer(Modifier.height(20.dp))
    AuthField(
        label = "E-mail",
        placeholder = "✉  usuario@afya.br",
        value = email,
        onValueChange = { email = it },
        password = false
    )
    Spacer(Modifier.height(12.dp))
    AuthField(
        label = "Senha",
        placeholder = "▣  ••••••••",
        value = password,
        onValueChange = { password = it },
        password = true,
        passwordVisible = passwordVisible,
        onToggleVisibility = { passwordVisible = !passwordVisible }
    )
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            rememberMe,
            { rememberMe = it },
            Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(checkedColor = Pink, uncheckedColor = FieldBorder)
        )
        Spacer(Modifier.width(7.dp))
        Text("Lembrar de mim", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Text(
            "Esqueceu a senha?",
            color = Pink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(Modifier.height(20.dp))
    PrimaryButton("Acessar Portal") {
        feedback = when {
            email.isBlank() || password.isBlank() -> "Preencha e-mail e senha para continuar."
            !email.contains("@") -> "Informe um e-mail válido."
            else -> "Login realizado com sucesso!"
        }
    }
    FeedbackMessage(feedback)
    Spacer(Modifier.height(44.dp))
    BottomLink("Novo por aqui?", "Criar conta", onCreateAccount)
}

@Composable
private fun RegistrationForm(onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    FormTitle("Crie sua conta acadêmica", "Cadastre-se para gerenciar seus projetos")
    Spacer(Modifier.height(20.dp))
    AuthField(
        label = "E-mail",
        placeholder = "✉  usuario@afya.br",
        value = email,
        onValueChange = { email = it },
        password = false,
        borderColor = Pink
    )
    Spacer(Modifier.height(12.dp))
    AuthField(
        label = "Senha",
        placeholder = "▣  ••••••••",
        value = password,
        onValueChange = { password = it },
        password = true,
        passwordVisible = passwordVisible,
        onToggleVisibility = { passwordVisible = !passwordVisible }
    )
    Spacer(Modifier.height(12.dp))
    AuthField(
        label = "Confirmar Senha",
        placeholder = "▣  ••••••••",
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        password = true,
        passwordVisible = confirmVisible,
        trailing = "✓",
        onToggleVisibility = { confirmVisible = !confirmVisible }
    )
    Spacer(Modifier.height(20.dp))
    PrimaryButton("Criar Conta") {
        feedback = when {
            email.isBlank() || password.isBlank() -> "Preencha todos os campos."
            !email.contains("@") -> "Informe um e-mail válido."
            password != confirmPassword -> "As senhas não coincidem."
            password.length < 6 -> "A senha deve ter ao menos 6 caracteres."
            else -> "Conta criada com sucesso!"
        }
    }
    FeedbackMessage(feedback)
    Spacer(Modifier.height(12.dp))
    Text(
        "Ao cadastrar-se, você concorda com as diretrizes e\ntermos acadêmicos da instituição Afya.",
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF96919A),
        fontSize = 9.sp,
        lineHeight = 14.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    Spacer(Modifier.height(44.dp))
    BottomLink("Já tem uma conta?", "Entrar", onLogin)
}

@Composable
private fun FeedbackMessage(message: String?) {
    if (message == null) return
    val isSuccess = message.endsWith("sucesso!")
    Spacer(Modifier.height(10.dp))
    Text(
        message,
        color = if (isSuccess) Color(0xFF1DBF8A) else Pink,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
private fun FormTitle(title: String, subtitle: String) {
    Text(
        title,
        color = Ink,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    ); Spacer(Modifier.height(3.dp)); Text(subtitle, color = Muted, fontSize = 11.sp)
}


@Composable
private fun AuthField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    password: Boolean,
    passwordVisible: Boolean = false,
    trailing: String? = null,
    onToggleVisibility: (() -> Unit)? = null,
    borderColor: Color = FieldBorder
) {
    Text(
        label,
        color = Ink,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    ); Spacer(Modifier.height(5.dp))
    OutlinedTextField(
        value,
        onValueChange,
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        placeholder = { Text(placeholder, color = Color(0xFF8A8790), fontSize = 12.sp) },
        singleLine = true,
        visualTransformation = if (password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) {
            {
                Text(
                    trailing ?: if (passwordVisible) "◉" else "◌",
                    color = if (trailing != null) Color(0xFF1DBF8A) else Color(0xFFAAA7AE),
                    modifier = Modifier.clickable { onToggleVisibility?.invoke() })
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            cursorColor = Pink,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        shape = RoundedCornerShape(7.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
    )
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Pink)
    ) { Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun BottomLink(prefix: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            prefix,
            color = Color(0xFF605B64),
            fontSize = 12.sp
        ); Spacer(Modifier.width(4.dp)); Text(
        action,
        color = Pink,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(onClick = onClick)
    )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPreview() {
    ExtensionConnectAPPTheme(darkTheme = false, dynamicColor = false) { Login() }
}