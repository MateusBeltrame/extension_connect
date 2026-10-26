package br.com.mateushb.extensionconnectapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.mateushb.extensionconnectapp.BuildConfig
import br.com.mateushb.extensionconnectapp.data.local.dao.ProjetoDao
import br.com.mateushb.extensionconnectapp.data.local.dao.RegistroDiarioDao
import br.com.mateushb.extensionconnectapp.data.local.dao.UsuarioDao
import br.com.mateushb.extensionconnectapp.data.local.entity.EtapaEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.HistoricoEstagioEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.ProjetoEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.ProjetoParticipanteEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.RegistroDiarioEntity
import br.com.mateushb.extensionconnectapp.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        ProjetoEntity::class,
        EtapaEntity::class,
        ProjetoParticipanteEntity::class,
        HistoricoEstagioEntity::class,
        RegistroDiarioEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun projetoDao(): ProjetoDao

    abstract fun registroDiarioDao(): RegistroDiarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Contas e projetos de teste (senha conhecida). Só em build debug — o APK de release nunca
        // nasce com administrador de senha pública
        private val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val senhaHash = SeedDebug.gerarSenhaHashPadrao()
                SeedDebug.inserirUsuariosIniciais(db, senhaHash)
                SeedDebug.inserirProjetosExemplo(db, senhaHash)
                SeedDebug.inserirAlunosNovoProjeto(db, senhaHash)
                SeedDebug.inserirRegistrosDiario(db)
            }
        }

        // Spec 002: tabelas de leitura da tela Projetos. SQL idêntico ao gerado pelas entities
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `projetos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `nome_grupo` TEXT NOT NULL,
                        `descricao` TEXT NOT NULL,
                        `tema` TEXT NOT NULL,
                        `orientador_id` INTEGER NOT NULL,
                        `data_inicio` TEXT NOT NULL,
                        `data_fim_previsto` TEXT NOT NULL,
                        `estagio` TEXT NOT NULL,
                        `aplicacao_inicio` TEXT,
                        `aplicacao_fim` TEXT,
                        `aplicacao_descricao` TEXT,
                        `aguardando_ciencia_orientador` INTEGER NOT NULL,
                        FOREIGN KEY(`orientador_id`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_projetos_orientador_id` ON `projetos` (`orientador_id`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `etapas` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `projeto_id` INTEGER NOT NULL,
                        `nome` TEXT NOT NULL,
                        `data_prevista` TEXT NOT NULL,
                        `concluida` INTEGER NOT NULL,
                        `ordem` INTEGER NOT NULL,
                        FOREIGN KEY(`projeto_id`) REFERENCES `projetos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_etapas_projeto_id` ON `etapas` (`projeto_id`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `projeto_participantes` (
                        `projeto_id` INTEGER NOT NULL,
                        `aluno_id` INTEGER NOT NULL,
                        PRIMARY KEY(`projeto_id`, `aluno_id`),
                        FOREIGN KEY(`projeto_id`) REFERENCES `projetos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`aluno_id`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_projeto_participantes_projeto_id` ON `projeto_participantes` (`projeto_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_projeto_participantes_aluno_id` ON `projeto_participantes` (`aluno_id`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `historico_estagios` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `projeto_id` INTEGER NOT NULL,
                        `estagio` TEXT NOT NULL,
                        `autor_id` INTEGER NOT NULL,
                        `data` TEXT NOT NULL,
                        `justificativa` TEXT,
                        FOREIGN KEY(`projeto_id`) REFERENCES `projetos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`autor_id`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_historico_estagios_projeto_id` ON `historico_estagios` (`projeto_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_historico_estagios_autor_id` ON `historico_estagios` (`autor_id`)")

                // Banco debug já existente também recebe os projetos de exemplo
                if (BuildConfig.DEBUG) SeedDebug.inserirProjetosExemplo(db, SeedDebug.gerarSenhaHashPadrao())
            }
        }

        // Spec 003: schema sem mudança. Existe só para o banco debug já existente receber os alunos de
        // teste do wizard "Novo projeto"; no release é uma migração vazia
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (BuildConfig.DEBUG) SeedDebug.inserirAlunosNovoProjeto(db, SeedDebug.gerarSenhaHashPadrao())
            }
        }

        // Spec 005: diário do projeto. SQL idêntico ao gerado pela RegistroDiarioEntity
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `registros_diario` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `projeto_id` INTEGER NOT NULL,
                        `autor_id` INTEGER NOT NULL,
                        `data` TEXT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `etapa_id` INTEGER,
                        `texto` TEXT NOT NULL,
                        FOREIGN KEY(`projeto_id`) REFERENCES `projetos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`autor_id`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(`etapa_id`) REFERENCES `etapas`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_registros_diario_projeto_id` ON `registros_diario` (`projeto_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_registros_diario_autor_id` ON `registros_diario` (`autor_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_registros_diario_etapa_id` ON `registros_diario` (`etapa_id`)")

                // Banco debug já existente também recebe os registros de exemplo
                if (BuildConfig.DEBUG) {
                    try {
                        SeedDebug.inserirRegistrosDiario(db)
                    } catch (_: Exception) {
                        // Dados de teste nunca derrubam a migração de schema: o diário só fica sem exemplos
                    }
                }
            }
        }

        fun obterInstancia(context: Context): AppDatabase {
            // Confere de novo dentro do lock: duas threads podem ter visto INSTANCE nulo
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: construir(context).also { INSTANCE = it }
            }
        }

        // Sem fallbackToDestructiveMigration: no Room 2.7+ o parâmetro é dropAllTables e qualquer
        // chamada LIGA a migração destrutiva. Sem ela, uma Migration ausente lança exceção em vez de
        // apagar os dados (Artigo 4 da constituição)
        private fun construir(context: Context): AppDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "extension_connect_database.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            if (BuildConfig.DEBUG) builder.addCallback(SEED_CALLBACK)
            return builder.build()
        }
    }
}
