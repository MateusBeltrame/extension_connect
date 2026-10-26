package br.com.mateushb.extensionconnectapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.com.mateushb.extensionconnectapp.model.PerfilUsuario
import br.com.mateushb.extensionconnectapp.model.Usuario
import br.com.mateushb.extensionconnectapp.ui.state.CadastroUiState
import br.com.mateushb.extensionconnectapp.ui.state.CompletarCadastroUiState
import br.com.mateushb.extensionconnectapp.ui.state.LoginUiState
import br.com.mateushb.extensionconnectapp.ui.state.SalvarProjetoUiState
import br.com.mateushb.extensionconnectapp.ui.state.SessaoUiState
import br.com.mateushb.extensionconnectapp.ui.telas.AcessoScreen
import br.com.mateushb.extensionconnectapp.ui.telas.CompletarCadastroScreen
import br.com.mateushb.extensionconnectapp.ui.telas.EditarPessoaScreen
import br.com.mateushb.extensionconnectapp.ui.telas.EmBreveScreen
import br.com.mateushb.extensionconnectapp.ui.telas.NovoProjetoScreen
import br.com.mateushb.extensionconnectapp.ui.telas.PessoasListaScreen
import br.com.mateushb.extensionconnectapp.ui.telas.ProjetoDetalhesScreen
import br.com.mateushb.extensionconnectapp.ui.telas.ProjetosListaScreen
import br.com.mateushb.extensionconnectapp.ui.telas.RecuperarSenhaScreen
import br.com.mateushb.extensionconnectapp.ui.viewmodel.AutenticacaoViewModel
import br.com.mateushb.extensionconnectapp.ui.viewmodel.NovoProjetoViewModel
import br.com.mateushb.extensionconnectapp.ui.viewmodel.PessoaViewModel
import br.com.mateushb.extensionconnectapp.ui.viewmodel.ProjetoDetalhesViewModel
import br.com.mateushb.extensionconnectapp.ui.viewmodel.ProjetoViewModel

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

    // BottomBar condicional por perfil (06-navegacao.md): só nas rotas de topo do perfil logado
    val entradaAtual by navController.currentBackStackEntryAsState()
    val itensBottomBar = itensDaBottomBar(autenticacaoViewModel.usuarioLogado?.perfil)
    val destinoAtual = entradaAtual?.destination
    val mostrarBottomBar = itensBottomBar.any { it.estaSelecionado(destinoAtual) }

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                AppBottomBar(
                    itens = itensBottomBar,
                    destinoAtual = destinoAtual,
                    onItemClick = { item -> navegarLimpandoPilha(item.rota) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        // Insets ficam com cada tela (barras com gradiente por trás da status bar); o padding externo
        // é só a altura da BottomBar, que já inclui a barra de navegação do sistema
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        // Aplicado só nas telas de topo (ComBottomBar), não no NavHost inteiro: a tela que está saindo
        // no login/logout não muda de tamanho no meio da transição
        val alturaBottomBar = innerPadding.calculateBottomPadding()
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

                // Escopo da entrada da pilha: descartado em "Sair da conta" e ao trocar de item da
                // BottomBar (popUpTo do grafo), então nada da sessão anterior chega ao próximo usuário
                val projetoViewModel: ProjetoViewModel = viewModel()

                LaunchedEffect(usuario) {
                    projetoViewModel.carregarProjetos(usuario)
                }

                ComBottomBar(alturaBottomBar) {
                    ProjetosListaScreen(
                        usuario = usuario,
                        listaUiState = projetoViewModel.listaUiState,
                        busca = projetoViewModel.busca,
                        filtroAtual = projetoViewModel.filtroAtual,
                        onBuscaChange = { projetoViewModel.alterarBusca(it) },
                        onAplicarFiltro = { projetoViewModel.aplicarFiltro(it) },
                        onAlternarSouOrientador = { projetoViewModel.alternarSouOrientador() },
                        onAlternarSoParados = { projetoViewModel.alternarSoParados() },
                        onLimparFiltros = { projetoViewModel.limparFiltros() },
                        onTentarNovamente = { projetoViewModel.carregarProjetos(usuario) },
                        onAdicionarClick = {
                            navController.navigate(AppRoute.NovoProjeto) {
                                launchSingleTop = true
                            }
                        },
                        onProjetoClick = { projetoId ->
                            navController.navigate(AppRoute.ProjetoDetalhes(projetoId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable<AppRoute.NovoProjeto> { entrada ->
                val usuario = usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                    ?: return@composable

                // Aluno não cria projeto (spec 003): pilha restaurada com outro perfil volta para a lista
                if (usuario.perfil == PerfilUsuario.ALUNO) {
                    LaunchedEffect(Unit) { navController.popBackStack<AppRoute.NovoProjeto>(inclusive = true) }
                    return@composable
                }

                // Escopo da entrada: o rascunho do wizard morre quando ela sai da pilha
                val novoProjetoViewModel: NovoProjetoViewModel = viewModel()
                val salvarUiState = novoProjetoViewModel.salvarUiState

                LaunchedEffect(usuario) {
                    novoProjetoViewModel.iniciar(usuario)
                }

                // Salvou: abre os detalhes tirando o wizard da pilha (o voltar leva a Projetos)
                LaunchedEffect(salvarUiState) {
                    if (salvarUiState is SalvarProjetoUiState.Sucesso) {
                        novoProjetoViewModel.limparSalvarUiState()
                        navController.navigate(AppRoute.ProjetoDetalhes(salvarUiState.projetoId)) {
                            popUpTo<AppRoute.NovoProjeto> {
                                inclusive = true
                            }
                        }
                    }
                }

                NovoProjetoScreen(
                    formularioUiState = novoProjetoViewModel.formularioUiState,
                    salvarUiState = salvarUiState,
                    rascunho = novoProjetoViewModel.rascunho,
                    passoAtual = novoProjetoViewModel.passoAtual,
                    orientadorInicial = novoProjetoViewModel.orientadorInicial,
                    onRascunhoChange = { novoProjetoViewModel.alterarRascunho(it) },
                    onAvancarClick = { novoProjetoViewModel.avancarPasso() },
                    onVoltarClick = { novoProjetoViewModel.voltarPasso() },
                    onAdicionarEtapa = { novoProjetoViewModel.adicionarEtapa(it) },
                    onRemoverEtapa = { novoProjetoViewModel.removerEtapa(it) },
                    onMoverEtapa = { de, para -> novoProjetoViewModel.moverEtapa(de, para) },
                    onAdicionarParticipante = { novoProjetoViewModel.adicionarParticipante(it) },
                    onRemoverParticipante = { novoProjetoViewModel.removerParticipante(it) },
                    onSalvarClick = { novoProjetoViewModel.salvar(usuario) },
                    onFecharClick = { voltarSeAtiva(navController, entrada) },
                    onTentarNovamente = { novoProjetoViewModel.carregarDados() },
                    onErroMostrado = { novoProjetoViewModel.limparSalvarUiState() }
                )
            }

            composable<AppRoute.ProjetoDetalhes> { entrada ->
                val usuario = usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                    ?: return@composable
                val projetoId = entrada.toRoute<AppRoute.ProjetoDetalhes>().projetoId

                val detalhesViewModel: ProjetoDetalhesViewModel = viewModel()

                LaunchedEffect(projetoId) {
                    detalhesViewModel.carregarDetalhesProjeto(projetoId)
                    detalhesViewModel.carregarDiario(projetoId)
                    detalhesViewModel.carregarArquivos(projetoId)
                }

                // Permissões de estágio, ciência e etapa são conferidas com o usuário logado (spec 004)
                ProjetoDetalhesScreen(
                    usuario = usuario,
                    detalhesUiState = detalhesViewModel.detalhesUiState,
                    mudarEstagioUiState = detalhesViewModel.mudarEstagioUiState,
                    acaoUiState = detalhesViewModel.acaoUiState,
                    onVoltarClick = { voltarSeAtiva(navController, entrada) },
                    onTentarNovamente = { detalhesViewModel.carregarDetalhesProjeto(projetoId) },
                    onMudarEstagio = { transicao, aplicacao, justificativa ->
                        detalhesViewModel.mudarEstagio(usuario, transicao, aplicacao, justificativa)
                    },
                    onConfirmarCiencia = { detalhesViewModel.confirmarCiencia(usuario) },
                    onAlternarEtapa = { detalhesViewModel.alternarEtapa(usuario, it) },
                    onMudarEstagioMensagemMostrada = { detalhesViewModel.limparMudarEstagioUiState() },
                    onAcaoMensagemMostrada = { detalhesViewModel.limparAcaoUiState() },
                    // Diário (spec 005): quem publica e quem exclui também são conferidos com o usuário logado
                    listaRegistrosUiState = detalhesViewModel.listaRegistrosUiState,
                    salvarRegistroUiState = detalhesViewModel.salvarRegistroUiState,
                    exclusaoRegistroUiState = detalhesViewModel.exclusaoRegistroUiState,
                    onPublicarRegistro = { texto, etapaId -> detalhesViewModel.publicarRegistro(usuario, texto, etapaId) },
                    onExcluirRegistro = { detalhesViewModel.excluirRegistro(usuario, it) },
                    onTentarNovamenteDiario = { detalhesViewModel.carregarDiario(projetoId) },
                    onSalvarRegistroMensagemMostrada = { detalhesViewModel.limparSalvarRegistroUiState() },
                    onExclusaoRegistroMensagemMostrada = { detalhesViewModel.limparExclusaoRegistroUiState() },
                    // Arquivos (spec 006): quem envia, exclui e muda a autorização é conferido com o usuário logado
                    listaArquivosUiState = detalhesViewModel.listaArquivosUiState,
                    preparacaoFotoUiState = detalhesViewModel.preparacaoFotoUiState,
                    envioDocumentoUiState = detalhesViewModel.envioDocumentoUiState,
                    envioFotoUiState = detalhesViewModel.envioFotoUiState,
                    exclusaoArquivoUiState = detalhesViewModel.exclusaoArquivoUiState,
                    onEnviarDocumento = { detalhesViewModel.enviarDocumento(usuario, it) },
                    onPrepararFoto = { uri, daCamera -> detalhesViewModel.prepararFoto(usuario, uri, daCamera) },
                    onEnviarFoto = { detalhesViewModel.enviarFoto(usuario, it) },
                    onDescartarFoto = { detalhesViewModel.descartarFoto() },
                    onAlterarAutorizacaoMural = { fotoId, autorizada ->
                        detalhesViewModel.alterarAutorizacaoMural(usuario, fotoId, autorizada)
                    },
                    onExcluirDocumento = { detalhesViewModel.excluirDocumento(usuario, it) },
                    onExcluirFoto = { detalhesViewModel.excluirFoto(usuario, it) },
                    novaUriCaptura = { detalhesViewModel.novaUriCaptura() },
                    uriDoDocumento = { detalhesViewModel.uriDoDocumento(it) },
                    onTentarNovamenteArquivos = { detalhesViewModel.carregarArquivos(projetoId) },
                    onPreparacaoFotoErroMostrado = { detalhesViewModel.limparErroPreparacaoFoto() },
                    onEnvioDocumentoMensagemMostrada = { detalhesViewModel.limparEnvioDocumentoUiState() },
                    onEnvioFotoMensagemMostrada = { detalhesViewModel.limparEnvioFotoUiState() },
                    onExclusaoArquivoMensagemMostrada = { detalhesViewModel.limparExclusaoArquivoUiState() }
                )
            }

            // ============================
            // Agrupamento: Pessoas (spec 007)
            // ============================
            composable<AppRoute.PessoasLista> { entrada ->
                val usuario = usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                    ?: return@composable

                // Só Administrador: pilha restaurada com outro perfil volta para Projetos
                if (usuario.perfil != PerfilUsuario.ADMINISTRADOR) {
                    LaunchedEffect(Unit) { navegarLimpandoPilha(AppRoute.ProjetosLista) }
                    return@composable
                }

                val pessoaViewModel: PessoaViewModel = viewModel()
                // Snackbar de quem voltou de "Editar pessoa" depois de salvar (gravado no savedStateHandle
                // desta entrada); a lista em si é reativa e já mostra os dados novos
                val mensagem by entrada.savedStateHandle
                    .getStateFlow<String?>(CHAVE_MENSAGEM_PESSOAS, null)
                    .collectAsState()

                LaunchedEffect(Unit) {
                    pessoaViewModel.carregarPessoas()
                }

                ComBottomBar(alturaBottomBar) {
                    PessoasListaScreen(
                        listaUiState = pessoaViewModel.listaUiState,
                        busca = pessoaViewModel.busca,
                        filtroAtual = pessoaViewModel.filtroAtual,
                        mensagem = mensagem,
                        onBuscaChange = { pessoaViewModel.alterarBusca(it) },
                        onFiltroChange = { pessoaViewModel.aplicarFiltro(it) },
                        onPessoaClick = { usuarioId ->
                            navController.navigate(AppRoute.EditarPessoa(usuarioId)) {
                                launchSingleTop = true
                            }
                        },
                        onTentarNovamente = { pessoaViewModel.carregarPessoas() },
                        onMensagemMostrada = { entrada.savedStateHandle[CHAVE_MENSAGEM_PESSOAS] = null }
                    )
                }
            }

            composable<AppRoute.EditarPessoa> { entrada ->
                val usuario = usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                    ?: return@composable

                if (usuario.perfil != PerfilUsuario.ADMINISTRADOR) {
                    LaunchedEffect(Unit) { navegarLimpandoPilha(AppRoute.ProjetosLista) }
                    return@composable
                }

                val usuarioId = entrada.toRoute<AppRoute.EditarPessoa>().usuarioId
                val pessoaViewModel: PessoaViewModel = viewModel()

                // Na rotação o ViewModel ignora o mesmo id, para não apagar o que foi digitado
                LaunchedEffect(usuarioId) {
                    pessoaViewModel.carregarPessoa(usuarioId)
                }

                EditarPessoaScreen(
                    usuario = usuario,
                    formularioUiState = pessoaViewModel.formularioUiState,
                    salvarUiState = pessoaViewModel.salvarUiState,
                    onSalvarClick = { pessoaViewModel.salvar(usuario, it) },
                    onVoltarClick = { voltarSeAtiva(navController, entrada) },
                    onTentarNovamente = { pessoaViewModel.carregarPessoa(usuarioId) },
                    // O próprio cadastro (HU-05) chega à sessão sozinho: o AutenticacaoViewModel observa a linha
                    onSalvoComSucesso = {
                        pessoaViewModel.limparSalvarUiState()
                        // Só sai (e só avisa a lista) se esta ainda for a entrada do topo: senão a mensagem
                        // cairia numa entrada que não é a lista, ou num Snackbar fora de hora
                        if (navController.currentBackStackEntry?.id == entrada.id) {
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                CHAVE_MENSAGEM_PESSOAS, "Pessoa atualizada com sucesso"
                            )
                            navController.popBackStack()
                        }
                    },
                    onSalvarMensagemMostrada = { pessoaViewModel.limparSalvarUiState() },
                    onRecarregarBloqueios = { pessoaViewModel.recarregarBloqueios() }
                )
            }

            // ============================
            // Agrupamento: Perfil e Mural (provisórias)
            // ============================
            composable<AppRoute.MeuPerfil> { entrada ->
                usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                    ?: return@composable

                // "Sair da conta" fica aqui até a feature 8 (Meu perfil)
                ComBottomBar(alturaBottomBar) { EmBreveScreen(titulo = "Meu perfil", onSairClick = sair) }
            }

            composable<AppRoute.Mural> { entrada ->
                // Acesso sem login entra com a feature 9; por ora só é alcançado pela BottomBar
                usuarioDaRota(autenticacaoViewModel.usuarioLogado) { redirecionarSemSessao(entrada) }
                    ?: return@composable

                ComBottomBar(alturaBottomBar) { EmBreveScreen(titulo = "Mural") }
            }
        }
    }
}

// Snackbar "Pessoa atualizada com sucesso", passado de EditarPessoa para PessoasLista
private const val CHAVE_MENSAGEM_PESSOAS = "mensagemPessoas"

// ===== BottomBar =====

private enum class ItemBottomBar(val rota: AppRoute, val rotulo: String, val icone: ImageVector) {
    PROJETOS(AppRoute.ProjetosLista, "Projetos", Icons.Outlined.GridView),
    MURAL(AppRoute.Mural, "Mural", Icons.Outlined.WorkspacePremium),
    PESSOAS(AppRoute.PessoasLista, "Pessoas", Icons.Outlined.Group),
    PERFIL(AppRoute.MeuPerfil, "Perfil", Icons.Outlined.Person);

    fun estaSelecionado(destino: NavDestination?): Boolean = when (this) {
        PROJETOS -> destino?.hasRoute<AppRoute.ProjetosLista>() == true
        MURAL -> destino?.hasRoute<AppRoute.Mural>() == true
        PESSOAS -> destino?.hasRoute<AppRoute.PessoasLista>() == true
        PERFIL -> destino?.hasRoute<AppRoute.MeuPerfil>() == true
    }
}

// Sem sessão não há BottomBar; Pessoas só para Administrador
private fun itensDaBottomBar(perfil: PerfilUsuario?): List<ItemBottomBar> = when (perfil) {
    null -> emptyList()
    PerfilUsuario.ADMINISTRADOR -> ItemBottomBar.entries
    else -> ItemBottomBar.entries - ItemBottomBar.PESSOAS
}

// Altura da BottomBar como padding inferior de uma tela de topo. Guarda o último valor: no logout a
// barra some no início da transição e a tela que está saindo não deve pular. consumeWindowInsets evita
// que o Scaffold interno some de novo o inset da navegação do sistema
@Composable
private fun ComBottomBar(alturaBottomBar: Dp, content: @Composable () -> Unit) {
    val ultimaAltura = remember { UltimaAltura() }
    if (alturaBottomBar > 0.dp) ultimaAltura.valor = alturaBottomBar
    val padding = PaddingValues(bottom = ultimaAltura.valor)
    Box(
        modifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
    ) {
        content()
    }
}

// Não é State de propósito: só guarda o valor entre composições, sem disparar recomposição
private class UltimaAltura(var valor: Dp = 0.dp)

// Frame bottom-nav (33:2): fundo branco com borda superior, item ativo rosa com pílula rosa clara
@Composable
private fun AppBottomBar(
    itens: List<ItemBottomBar>,
    destinoAtual: NavDestination?,
    onItemClick: (ItemBottomBar) -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            itens.forEach { item ->
                val selecionado = item.estaSelecionado(destinoAtual)
                NavigationBarItem(
                    selected = selecionado,
                    onClick = { if (!selecionado) onItemClick(item) },
                    // O rótulo já descreve o item: descrição no ícone faria o TalkBack ler duas vezes
                    icon = { Icon(imageVector = item.icone, contentDescription = null) },
                    label = {
                        Text(
                            text = item.rotulo,
                            fontSize = 12.sp,
                            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
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

// Só a entrada ativa volta: um segundo toque durante a transição desempilharia a tela de baixo
private fun voltarSeAtiva(navController: NavHostController, entrada: NavBackStackEntry) {
    if (entrada.lifecycle.currentState == Lifecycle.State.RESUMED) {
        navController.popBackStack()
    }
}

// Aluno sem RA/curso passa pelo gate antes de entrar no app
private fun destinoAposLogin(usuario: Usuario): AppRoute =
    if (usuario.cadastroIncompleto) AppRoute.CompletarCadastro else AppRoute.ProjetosLista
