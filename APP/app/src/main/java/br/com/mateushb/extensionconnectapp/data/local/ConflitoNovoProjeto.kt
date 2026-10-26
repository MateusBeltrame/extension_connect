package br.com.mateushb.extensionconnectapp.data.local

// Lançada dentro de ProjetoDao.inserirProjetoCompleto: a exceção desfaz a transação, então a
// conferência e a gravação são atômicas. O ProjetoRepository converte em ResultadoSalvarProjeto
sealed class ConflitoNovoProjeto : Exception() {
    class OrientadorInativo : ConflitoNovoProjeto()
    class AlunoInativo(val alunoId: Int) : ConflitoNovoProjeto()
    class AlunoOcupado(val alunoId: Int, val nomeGrupo: String) : ConflitoNovoProjeto()
}
