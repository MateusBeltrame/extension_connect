package br.com.mateushb.extensionconnectapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.CadastroUiState
import br.com.mateushb.extensionconnectapp.ui.state.CompletarCadastroUiState
import br.com.mateushb.extensionconnectapp.ui.state.LoginUiState
import br.com.mateushb.extensionconnectapp.ui.state.SessaoUiState
import br.com.mateushb.extensionconnectapp.ui.telas.AcessoScreen
import br.com.mateushb.extensionconnectapp.ui.telas.CompletarCadastroScreen
import br.com.mateushb.extensionconnectapp.ui.telas.InicioScreen
import br.com.mateushb.extensionconnectapp.ui.telas.RecuperarSenhaScreen
import br.com.mateushb.extensionconnectapp.ui.viewmodel.AutenticacaoViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    autenticacaoViewModel: AutenticacaoViewModel = viewModel()
) {
    val sessaoUiState = autenticacaoViewModel.sessaoUiState

    // Enquanto a sessão lembrada é conferida no banco, ainda não se sabe o destino inicial
    if (sessaoUiState is SessaoUiState.Verificando) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Exceção documentada em 06-navegacao.md: o destino inicial depende da sessão (calculado uma vez)
    val destinoInicial: AppRoute = remember {
        when (sessaoUiState) {
            is SessaoUiState.Autenticado -> destinoAposLogin(sessaoUiState.usuario)
            else -> AppRoute.Acesso
        }
    }

    // Login, cadastro, gate e sair limpam toda a pilha: não se volta para a tela anterior
    val navegarLimpandoPilha: (AppRoute) -> Unit = { destino ->
        navController.navigate(destino) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    // Navega antes de limpar a sessão: a tela que sai continua com o usuário durante a transição
    val sair: () -> Unit = {
        navegarLimpandoPilha(AppRoute.Acesso)
        autenticacaoViewModel.sair()
    }

    // Rota autenticada sem sessão (ex.: pilha restaurada após morte do processo com a conta já
    // desativada): volta para Acesso. Só a entrada atual redireciona — a que está saindo após
    // "Sair da conta" não dispara uma segunda navegação
    val redirecionarSemSessao: (NavBackStackEntry) -> Unit = { entrada ->
        if (navController.currentBackStackEntry?.id == entrada.id) {
            navegarLimpandoPilha(AppRoute.Acesso)
        }
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {
        // ============================
        // Agrupamento: Autenticação
        // ============================
        composable<AppRoute.Acesso> {
            val loginUiState = autenticacaoViewModel.loginUiState
            val cadastroUiState = autenticacaoViewModel.cadastroUiState

            LaunchedEffect(loginUiState, cadastroUiState) {
                if (loginUiState is LoginUiState.Sucesso || cadastroUiState is CadastroUiState.Sucesso) {
                    autenticacaoViewModel.limparLoginUiState()
                    autenticacaoViewModel.limparCadastroUiState()
                    autenticacaoViewModel.usuarioLogado?.let { navegarLimpandoPilha(destinoAposLogin(it)) }
                }
            }

            AcessoScreen(
                loginUiState = loginUiState,
                cadastroUiState = cadastroUiState,
                onEntrarClick = { email, senha ->
                    autenticacaoViewModel.entrar(email, senha)
                },
                onCadastrarClick = { nome, email, senha ->
                    autenticacaoViewModel.cadastrar(nome, email, senha)
                },
                onEsqueceuSenhaClick = {
                    // Cada visita começa no formulário; launchSingleTop evita empilhar com toque duplo
                    autenticacaoViewModel.limparRecuperarSenhaUiState()
                    navController.navigate(AppRoute.RecuperarSenha) {
                        launchSingleTop = true
                    }
                },
                onLoginErroDispensado = {
                    autenticacaoViewModel.limparLoginUiState()
                },
                onCadastroErroDispensado = {
                    autenticacaoViewModel.limparCadastroUiState()
                }
            )
        }

        composable<AppRoute.RecuperarSenha> { entrada ->
            RecuperarSenhaScreen(
                recuperarSenhaUiState = autenticacaoViewModel.recuperarSenhaUiState,
                onEnviarClick = { email ->
                    autenticacaoViewModel.enviarLinkRecuperacao(email)
                },
                onVoltarClick = {
                    // Só a entrada ativa volta: um segundo toque durante a transição desempilharia Acesso
                    if (entrada.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable<AppRoute.CompletarCadastro> { entrada ->
            val usuario = usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                ?: return@composable
            val completarCadastroUiState = autenticacaoViewModel.completarCadastroUiState

            LaunchedEffect(completarCadastroUiState) {
                if (completarCadastroUiState is CompletarCadastroUiState.Sucesso) {
                    autenticacaoViewModel.limparCompletarCadastroUiState()
                    navegarLimpandoPilha(AppRoute.ProjetosLista)
                }
            }

            CompletarCadastroScreen(
                usuario = usuario,
                completarCadastroUiState = completarCadastroUiState,
                onSalvarClick = { ra, curso ->
                    autenticacaoViewModel.completarCadastro(ra, curso)
                },
                onSairClick = sair,
                onErroMostrado = {
                    autenticacaoViewModel.limparCompletarCadastroUiState()
                }
            )
        }

        // ============================
        // Agrupamento: Projetos
        // ============================
        composable<AppRoute.ProjetosLista> { entrada ->
            val usuario = usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                ?: return@composable

            // Tela provisória até a feature 2 (Projetos — lista e quadro)
            InicioScreen(
                usuario = usuario,
                onSairClick = sair
            )
        }
    }
}

// Usuário para uma rota autenticada: mantém o último usuário enquanto a tela sai (após "Sair da conta")
// e chama onSemSessao quando não há sessão
@Composable
private fun usuarioDaRota(usuarioLogado: Usuario?, onSemSessao: () -> Unit): Usuario? {
    val ultimoUsuario = remember { mutableStateOf(usuarioLogado) }
    SideEffect {
        if (usuarioLogado != null) ultimoUsuario.value = usuarioLogado
    }
    LaunchedEffect(usuarioLogado == null) {
        if (usuarioLogado == null) onSemSessao()
    }
    return usuarioLogado ?: ultimoUsuario.value
}

// Aluno sem RA/curso passa pelo gate antes de entrar no app
private fun destinoAposLogin(usuario: Usuario): AppRoute =
    if (usuario.cadastroIncompleto) AppRoute.CompletarCadastro else AppRoute.ProjetosLista
