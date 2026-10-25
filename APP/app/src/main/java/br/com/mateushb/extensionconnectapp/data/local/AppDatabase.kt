package br.com.mateushb.extensionconnectapp.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.mateushb.extensionconnectapp.BuildConfig
import br.com.mateushb.extensionconnectapp.data.local.dao.UsuarioDao
import br.com.mateushb.extensionconnectapp.data.local.entity.UsuarioEntity

@Database(
    entities = [UsuarioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val SENHA_PADRAO_SEED = "123456"

        // Contas de teste (senha conhecida): o cadastro só cria alunos e a tela de Pessoas ainda não
        // existe. Só em build debug — o APK de release nunca nasce com administrador de senha pública
        private val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                inserirUsuarioSeed(db, "Administrador", "admin@afya.br", "ADMINISTRADOR", ativo = true)
                inserirUsuarioSeed(db, "Professor Demo", "professor@afya.br", "PROFESSOR", ativo = true)
                inserirUsuarioSeed(
                    db, "Aluno Inativo", "inativo@afya.br", "ALUNO", ativo = false,
                    ra = "2023000001", curso = "ENGENHARIA_SOFTWARE"
                )
            }
        }

        private fun inserirUsuarioSeed(
            db: SupportSQLiteDatabase,
            nome: String,
            email: String,
            perfil: String,
            ativo: Boolean,
            ra: String? = null,
            curso: String? = null
        ) {
            val valores = ContentValues().apply {
                put("nome", nome)
                put("email", email)
                put("senha_hash", HashSenha.gerar(SENHA_PADRAO_SEED))
                put("perfil", perfil)
                put("ativo", if (ativo) 1 else 0)
                put("ra", ra)
                put("curso", curso)
            }
            db.insert("usuarios", SQLiteDatabase.CONFLICT_ABORT, valores)
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
            if (BuildConfig.DEBUG) builder.addCallback(SEED_CALLBACK)
            return builder.build()
        }
    }
}
