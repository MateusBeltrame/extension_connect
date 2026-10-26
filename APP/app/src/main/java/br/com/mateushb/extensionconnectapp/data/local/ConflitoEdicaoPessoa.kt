package br.com.mateushb.extensionconnectapp.data.local

import br.com.mateushb.extensionconnectapp.model.BloqueioEdicaoPessoa

// Lançada dentro de UsuarioDao.salvarEdicao: a exceção desfaz a transação, então a conferência e a
// gravação são atômicas. O UsuarioRepository converte em ResultadoSalvarPessoa
sealed class ConflitoEdicaoPessoa : Exception() {
    class NaoEncontrada : ConflitoEdicaoPessoa()
    class EdicaoPropria : ConflitoEdicaoPessoa()
    class Bloqueada(val bloqueio: BloqueioEdicaoPessoa) : ConflitoEdicaoPessoa()
}
