package br.com.mateushb.extensionconnectapp.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

// Dados de teste, SÓ em build debug (quem chama confere BuildConfig.DEBUG): contas com senha
// conhecida e os projetos dos frames do Figma (spec 002). Grava por SQL porque roda dentro do
// Callback.onCreate / da Migration, antes de os DAOs estarem disponíveis
object SeedDebug {

    private const val SENHA_PADRAO = "123456"

    // PBKDF2 com 600 000 iterações custa caro: quem chama gera uma vez e repassa para todas as contas
    fun gerarSenhaHashPadrao(): String = HashSenha.gerar(SENHA_PADRAO)

    // As 3 contas da spec 001 (banco novo)
    fun inserirUsuariosIniciais(db: SupportSQLiteDatabase, senhaHash: String) {
        inserirUsuario(db, senhaHash, "Administrador", "admin@alunos.afya.com.br", "ADMINISTRADOR")
        inserirUsuario(db, senhaHash, "Professor Demo", "professor@alunos.afya.com.br", "PROFESSOR")
        inserirUsuario(
            db, senhaHash, "Aluno Inativo", "inativo@alunos.afya.com.br", "ALUNO", ativo = false,
            ra = "2023000001", curso = "ENGENHARIA_SOFTWARE"
        )
    }

    // Projetos de exemplo com datas relativas a hoje, para "atrasado", "parado" e "esta semana"
    // sempre se reproduzirem. Pode rodar num banco que já tem usuários (migração 1 → 2)
    fun inserirProjetosExemplo(db: SupportSQLiteDatabase, senhaHash: String) {
        val hoje = LocalDate.now()

        PROFESSORES.forEach { (nome, email) -> inserirUsuario(db, senhaHash, nome, email, "PROFESSOR") }
        ALUNOS.forEachIndexed { indice, (nome, email) ->
            inserirUsuario(
                db, senhaHash, nome, email, "ALUNO",
                ra = "2024%06d".format(indice + 1), curso = "ENGENHARIA_SOFTWARE"
            )
        }

        // Grupo Raízes: em aplicação, aplicação já encerrada → atrasado; 3/5 etapas; 5 participantes
        inserirProjeto(
            db, hoje,
            titulo = "Horta comunitária na Escola Municipal Vila Nova",
            nomeGrupo = "Raízes",
            tema = "MEIO_AMBIENTE",
            orientador = "ana.ribeiro@afya.br",
            inicio = -45, fimPrevisto = 30,
            estagio = "EM_APLICACAO",
            aplicacao = -30 to -2,
            historico = listOf("EM_ANALISE" to -45, "EM_APLICACAO" to -12),
            etapas = listOf(true, true, true, false, false),
            participantes = listOf(
                "aluno@afya.br", "julia.lima@afya.br", "carla.rocha@afya.br",
                "igor.nunes@afya.br", "helena.dias@afya.br"
            )
        )
        // Grupo Letras Vivas: em análise, sem cronograma
        inserirProjeto(
            db, hoje,
            titulo = "Clube de leitura na biblioteca comunitária",
            nomeGrupo = "Letras Vivas",
            tema = "CULTURA",
            orientador = "ana.ribeiro@afya.br",
            inicio = -5, fimPrevisto = 90,
            estagio = "EM_ANALISE",
            historico = listOf("EM_ANALISE" to -5),
            participantes = listOf("marina.souza@afya.br", "paulo.lopes@afya.br")
        )
        // Grupo Sorriso: finalizado, aguardando ciência do orientador; 5/5 etapas
        inserirProjeto(
            db, hoje,
            titulo = "Saúde bucal nas creches municipais",
            nomeGrupo = "Sorriso",
            tema = "SAUDE",
            orientador = "juliana.torres@afya.br",
            inicio = -120, fimPrevisto = -10,
            estagio = "FINALIZADO",
            aplicacao = -50 to -20,
            historico = listOf("EM_ANALISE" to -120, "EM_APLICACAO" to -60, "FINALIZADO" to -3),
            etapas = listOf(true, true, true, true, true),
            participantes = listOf(
                "rafael.santos@afya.br", "tatiana.alves@afya.br", "gabriel.moura@afya.br", "aluno@afya.br"
            ),
            aguardandoCiencia = true
        )
        // Grupo Conecta: em análise há mais de 30 dias → parado; 0/4 etapas
        inserirProjeto(
            db, hoje,
            titulo = "Oficinas de informática para idosos do Bairro Alvorada",
            nomeGrupo = "Conecta",
            tema = "TECNOLOGIA",
            orientador = "carlos.mendes@afya.br",
            inicio = -34, fimPrevisto = 60,
            estagio = "EM_ANALISE",
            historico = listOf("EM_ANALISE" to -34),
            etapas = listOf(false, false, false, false),
            participantes = listOf("bruna.costa@afya.br", "lucas.ferreira@afya.br", "ana.pires@afya.br")
        )
        // Grupo Futuro: em aplicação cobrindo a semana atual; 2/6 etapas
        inserirProjeto(
            db, hoje,
            titulo = "Educação financeira para jovens aprendizes",
            nomeGrupo = "Futuro",
            tema = "EDUCACAO",
            orientador = "carlos.mendes@afya.br",
            inicio = -40, fimPrevisto = 45,
            estagio = "EM_APLICACAO",
            aplicacao = -3 to 20,
            historico = listOf("EM_ANALISE" to -40, "EM_APLICACAO" to -8),
            etapas = listOf(true, true, false, false, false, false),
            participantes = listOf("diego.vieira@afya.br", "karina.santos@afya.br", "leonardo.martins@afya.br")
        )
    }

    // Spec 003: alunos para testar a busca e o limite de 10 participantes do wizard "Novo projeto"
    // (nomes dos frames 37:267/37:393) e Luciana Freitas presa ao Grupo Sementes, para aparecer
    // bloqueada. Pode rodar num banco que já tem os projetos de exemplo (migração 2 → 3)
    fun inserirAlunosNovoProjeto(db: SupportSQLiteDatabase, senhaHash: String) {
        val hoje = LocalDate.now()
        val cursos = listOf(
            "ENGENHARIA_SOFTWARE", "MEDICINA", "ENFERMAGEM", "DIREITO", "ADMINISTRACAO",
            "CIENCIA_COMPUTACAO", "ODONTOLOGIA", "SISTEMAS_INFORMACAO", "ANALISE_DESENVOLVIMENTO_SISTEMAS"
        )
        ALUNOS_NOVO_PROJETO.forEachIndexed { indice, (nome, email) ->
            inserirUsuario(
                db, senhaHash, nome, email, "ALUNO",
                ra = "2025%06d".format(indice + 1), curso = cursos[indice % cursos.size]
            )
        }

        // Idempotente: o mesmo banco pode receber o seed pelo onCreate e não deve duplicar o projeto
        val sementesExiste = db.query("SELECT 1 FROM projetos WHERE nome_grupo = ?", arrayOf("Sementes"))
            .use { it.moveToFirst() }
        if (sementesExiste) return

        // Grupo Sementes: em análise; Luciana fica bloqueada na busca do Passo 3
        inserirProjeto(
            db, hoje,
            titulo = "Viveiro de mudas nativas no Parque Municipal",
            nomeGrupo = "Sementes",
            tema = "MEIO_AMBIENTE",
            orientador = "carlos.mendes@afya.br",
            inicio = -7, fimPrevisto = 80,
            estagio = "EM_ANALISE",
            historico = listOf("EM_ANALISE" to -7),
            etapas = listOf(false, false),
            participantes = listOf("luciana.freitas@afya.br")
        )
    }

    // Spec 005: registros do frame 35:2 no Grupo Raízes, uma atividade no Futuro e uma no Sorriso
    // (finalizado, para conferir o modo leitura). Conecta fica sem registros e continua parado.
    // Pode rodar num banco que já tem os projetos (migração 3 → 4)
    fun inserirRegistrosDiario(db: SupportSQLiteDatabase) {
        // Idempotente: onCreate e migração não duplicam o diário
        val jaTemRegistros = db.query("SELECT 1 FROM registros_diario LIMIT 1").use { it.moveToFirst() }
        if (jaTemRegistros) return

        val agora = LocalDateTime.now()
        val hoje = agora.toLocalDate()
        // "Hoje, 14:20" do Figma, sem ficar no futuro quando o seed roda de manhã
        val hojeAs1420 = minOf(hoje.atTime(14, 20), agora.minusMinutes(1))

        inserirRegistro(
            db, "Raízes", "julia.lima@afya.br", hoje.minusDays(12).atTime(10, 30), "ATIVIDADE", "Aplicação",
            "Reunião com a coordenação pedagógica para definir os horários das próximas oficinas."
        )
        inserirRegistro(
            db, "Raízes", "ana.ribeiro@afya.br", hoje.minusDays(1).atTime(18, 5), "ORIENTACAO", "Relatório final",
            "Lembrem de registrar fotos autorizadas das oficinas e começar o relatório final até sexta."
        )
        inserirRegistro(
            db, "Raízes", "aluno@afya.br", hojeAs1420, "ATIVIDADE", "Aplicação",
            "Realizamos a segunda oficina de plantio com a turma do 4º ano. As mudas de alface e cenoura " +
                "foram para os canteiros."
        )
        inserirRegistro(
            db, "Futuro", "diego.vieira@afya.br", hoje.minusDays(2).atTime(16, 0), "ATIVIDADE", "Aplicação",
            "Primeiro encontro com os jovens aprendizes: apresentamos o orçamento pessoal e a planilha de gastos."
        )
        inserirRegistro(
            db, "Sorriso", "aluno@afya.br", hoje.minusDays(25).atTime(9, 15), "ATIVIDADE", null,
            "Entregamos os kits de escovação nas três creches e combinamos a visita de retorno."
        )
    }

    // Projeto, autor e etapa pelo nome/e-mail; sem o projeto ou o autor (seed de outra versão, conta
    // alterada à mão), não grava. Não usa idPorEmail, que lança exceção e abortaria a migração
    private fun inserirRegistro(
        db: SupportSQLiteDatabase,
        nomeGrupo: String,
        autor: String,
        momento: LocalDateTime,
        tipo: String,
        etapa: String?,
        texto: String
    ) {
        val projetoId = db.query("SELECT id FROM projetos WHERE nome_grupo = ?", arrayOf(nomeGrupo))
            .use { if (it.moveToFirst()) it.getLong(0) else null } ?: return
        val autorId = db.query("SELECT id FROM usuarios WHERE email = ?", arrayOf(autor))
            .use { if (it.moveToFirst()) it.getLong(0) else null } ?: return
        val etapaId = etapa?.let { nome ->
            db.query("SELECT id FROM etapas WHERE projeto_id = ? AND nome = ?", arrayOf<Any>(projetoId, nome))
                .use { if (it.moveToFirst()) it.getLong(0) else null }
        }
        val registro = ContentValues().apply {
            put("projeto_id", projetoId)
            put("autor_id", autorId)
            put("data", momento.atZone(ZoneId.systemDefault()).toInstant().toString())
            put("tipo", tipo)
            put("etapa_id", etapaId)
            put("texto", texto)
        }
        db.insert("registros_diario", SQLiteDatabase.CONFLICT_ABORT, registro)
    }

    private val ALUNOS_NOVO_PROJETO = listOf(
        "Lucas Moura" to "lucas.moura@afya.br",
        "Lúcio Carvalho" to "lucio.carvalho@afya.br",
        "Caio Rocha" to "caio.rocha@afya.br",
        "Bianca Nunes" to "bianca.nunes@afya.br",
        "Diego Vaz" to "diego.vaz@afya.br",
        "Karen Souza" to "karen.souza@afya.br",
        "Paula Lins" to "paula.lins@afya.br",
        "Rafael Dias" to "rafael.dias@afya.br",
        "Tânia Alves" to "tania.alves@afya.br",
        "Luciana Freitas" to "luciana.freitas@afya.br"
    )

    private val PROFESSORES = listOf(
        "Ana Ribeiro" to "ana.ribeiro@afya.br",
        "Carlos Mendes" to "carlos.mendes@afya.br",
        "Juliana Torres" to "juliana.torres@afya.br"
    )

    private val ALUNOS = listOf(
        "Mateus Silva" to "aluno@afya.br",
        "Julia Lima" to "julia.lima@afya.br",
        "Carla Rocha" to "carla.rocha@afya.br",
        "Igor Nunes" to "igor.nunes@afya.br",
        "Helena Dias" to "helena.dias@afya.br",
        "Marina Souza" to "marina.souza@afya.br",
        "Paulo Lopes" to "paulo.lopes@afya.br",
        "Rafael Santos" to "rafael.santos@afya.br",
        "Tatiana Alves" to "tatiana.alves@afya.br",
        "Gabriel Moura" to "gabriel.moura@afya.br",
        "Bruna Costa" to "bruna.costa@afya.br",
        "Lucas Ferreira" to "lucas.ferreira@afya.br",
        "Ana Paula Pires" to "ana.pires@afya.br",
        "Diego Vieira" to "diego.vieira@afya.br",
        "Karina Santos" to "karina.santos@afya.br",
        "Leonardo Martins" to "leonardo.martins@afya.br"
    )

    private val NOMES_ETAPAS = listOf(
        "Planejamento", "Preparação de materiais", "Aplicação", "Relatório final", "Apresentação", "Avaliação"
    )

    // IGNORE: e-mail já cadastrado (índice único) mantém a conta existente
    private fun inserirUsuario(
        db: SupportSQLiteDatabase,
        senhaHash: String,
        nome: String,
        email: String,
        perfil: String,
        ativo: Boolean = true,
        ra: String? = null,
        curso: String? = null
    ) {
        val valores = ContentValues().apply {
            put("nome", nome)
            put("email", email)
            put("senha_hash", senhaHash)
            put("perfil", perfil)
            put("ativo", if (ativo) 1 else 0)
            put("ra", ra)
            put("curso", curso)
        }
        db.insert("usuarios", SQLiteDatabase.CONFLICT_IGNORE, valores)
    }

    private fun idPorEmail(db: SupportSQLiteDatabase, email: String): Long =
        db.query("SELECT id FROM usuarios WHERE email = ?", arrayOf(email)).use { cursor ->
            check(cursor.moveToFirst()) { "Usuário do seed não encontrado: $email" }
            cursor.getLong(0)
        }

    // Datas como deslocamento em dias a partir de hoje
    private fun inserirProjeto(
        db: SupportSQLiteDatabase,
        hoje: LocalDate,
        titulo: String,
        nomeGrupo: String,
        tema: String,
        orientador: String,
        inicio: Int,
        fimPrevisto: Int,
        estagio: String,
        historico: List<Pair<String, Int>>,
        participantes: List<String>,
        aplicacao: Pair<Int, Int>? = null,
        etapas: List<Boolean> = emptyList(),
        aguardandoCiencia: Boolean = false
    ) {
        val orientadorId = idPorEmail(db, orientador)
        val projeto = ContentValues().apply {
            put("titulo", titulo)
            put("nome_grupo", nomeGrupo)
            put("descricao", "Projeto de exemplo gerado para testes (build debug).")
            put("tema", tema)
            put("orientador_id", orientadorId)
            put("data_inicio", hoje.plusDays(inicio.toLong()).toString())
            put("data_fim_previsto", hoje.plusDays(fimPrevisto.toLong()).toString())
            put("estagio", estagio)
            put("aplicacao_inicio", aplicacao?.let { hoje.plusDays(it.first.toLong()).toString() })
            put("aplicacao_fim", aplicacao?.let { hoje.plusDays(it.second.toLong()).toString() })
            put("aplicacao_descricao", aplicacao?.let { "Aplicação junto à comunidade." })
            put("aguardando_ciencia_orientador", if (aguardandoCiencia) 1 else 0)
        }
        val projetoId = db.insert("projetos", SQLiteDatabase.CONFLICT_ABORT, projeto)

        participantes.forEach { email ->
            val vinculo = ContentValues().apply {
                put("projeto_id", projetoId)
                put("aluno_id", idPorEmail(db, email))
            }
            db.insert("projeto_participantes", SQLiteDatabase.CONFLICT_IGNORE, vinculo)
        }

        etapas.forEachIndexed { indice, concluida ->
            val etapa = ContentValues().apply {
                put("projeto_id", projetoId)
                put("nome", NOMES_ETAPAS[indice % NOMES_ETAPAS.size])
                put("data_prevista", hoje.plusDays(inicio + (indice + 1) * 10L).toString())
                put("concluida", if (concluida) 1 else 0)
                put("ordem", indice)
            }
            db.insert("etapas", SQLiteDatabase.CONFLICT_ABORT, etapa)
        }

        historico.forEach { (estagioAlcancado, dias) ->
            val linha = ContentValues().apply {
                put("projeto_id", projetoId)
                put("estagio", estagioAlcancado)
                // Autor: o primeiro participante cria; o orientador move os estágios seguintes
                put("autor_id", if (estagioAlcancado == "EM_ANALISE") idPorEmail(db, participantes.first()) else orientadorId)
                put("data", instanteDoDia(hoje.plusDays(dias.toLong())))
            }
            db.insert("historico_estagios", SQLiteDatabase.CONFLICT_ABORT, linha)
        }
    }

    private fun instanteDoDia(dia: LocalDate): String =
        dia.atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant().toString()
}
