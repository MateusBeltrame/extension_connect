package br.com.mateushb.extensionconnectapp.data.local.dao

// Trechos de SQL das regras de permissão, compartilhados pelos DAOs do diário, dos documentos e das
// fotos: mudar uma regra (novo perfil, novo bloqueio) muda nos três de uma vez. Parâmetros esperados
// na consulta: :autorId e, em PODE_ENVIAR_ARQUIVOS, :projetoId
internal object RegrasPermissao {

    // Desativar a conta tira os direitos mesmo com a sessão aberta
    const val AUTOR_ATIVO = "EXISTS (SELECT 1 FROM usuarios u WHERE u.id = :autorId AND u.ativo = 1)"

    // Para DELETE/UPDATE na tabela do item (coluna projeto_id)
    const val PROJETO_NAO_FINALIZADO = "projeto_id IN (SELECT id FROM projetos WHERE estagio != 'FINALIZADO')"

    // Projeto p não finalizado e conta ativa (início do WHERE sobre projetos p)
    const val PROJETO_ABERTO_E_AUTOR_ATIVO = "p.id = :projetoId AND p.estagio != 'FINALIZADO' AND $AUTOR_ATIVO"

    const val E_PARTICIPANTE =
        "EXISTS (SELECT 1 FROM projeto_participantes pp WHERE pp.projeto_id = p.id AND pp.aluno_id = :autorId)"

    // Documento e foto: participante ou orientador (spec 006, Permissões)
    const val PODE_ENVIAR_ARQUIVOS =
        "SELECT COUNT(*) FROM projetos p WHERE $PROJETO_ABERTO_E_AUTOR_ATIVO " +
            "AND (p.orientador_id = :autorId OR $E_PARTICIPANTE)"
}
