package br.com.mateushb.extensionconnectapp.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import br.com.mateushb.extensionconnectapp.model.Arquivo
import br.com.mateushb.extensionconnectapp.model.FormatoDocumento
import br.com.mateushb.extensionconnectapp.model.FotoPreparada
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

// Cópia local dos arquivos da aba Arquivos (spec 006). Sem backend, o arquivo "enviado" é a cópia no
// armazenamento do app: documentos em files/arquivos/, fotos comprimidas em files/fotos/. A foto passa
// por cache/fotos/ enquanto espera o sheet de autorização; a câmera grava em cache/camera/.
// Operações de arquivo bloqueiam: quem chama roda em Dispatchers.IO
class ArmazenamentoArquivos(context: Context) {

    private val contexto = context.applicationContext
    private val resolver = contexto.contentResolver
    private val autoridade = "${contexto.packageName}.arquivos"

    // Documento validado e já copiado para files/arquivos/
    data class DocumentoCopiado(
        val nomeArquivo: String,
        val formato: FormatoDocumento,
        val tamanhoBytes: Long,
        val caminho: String
    )

    // ===== Documentos =====

    // Formato pela extensão do nome (ou pelo tipo informado pelo app de origem) e até 10 MB, contando
    // os bytes durante a cópia: o tamanho informado pelo provedor pode faltar
    fun copiarDocumento(uri: Uri): DocumentoCopiado {
        val (nomeOriginal, tamanhoInformado) = consultarNomeETamanho(uri)
        val nome = nomeOriginal ?: "documento"
        val formatoPeloNome = FormatoDocumento.doNome(nome)
        val formato = formatoPeloNome
            ?: FormatoDocumento.entries.firstOrNull { it.mime == resolver.getType(uri) }
            ?: throw ArquivoRecusado(MENSAGEM_FORMATO_DOCUMENTO)
        val nomeArquivo = if (formatoPeloNome == null) "$nome.${formato.name.lowercase()}" else nome
        if (tamanhoInformado != null && tamanhoInformado > Arquivo.TAMANHO_MAXIMO) {
            throw ArquivoRecusado(MENSAGEM_TAMANHO_DOCUMENTO)
        }

        val destino = File(pasta(contexto.filesDir, PASTA_ARQUIVOS), "${UUID.randomUUID()}.${formato.name.lowercase()}")
        try {
            val entrada = resolver.openInputStream(uri) ?: throw ArquivoRecusado(MENSAGEM_ARQUIVO_ILEGIVEL)
            entrada.use { origem ->
                destino.outputStream().use { saida ->
                    val buffer = ByteArray(BUFFER)
                    var total = 0L
                    while (true) {
                        val lidos = origem.read(buffer)
                        if (lidos < 0) break
                        total += lidos
                        if (total > Arquivo.TAMANHO_MAXIMO) throw ArquivoRecusado(MENSAGEM_TAMANHO_DOCUMENTO)
                        saida.write(buffer, 0, lidos)
                    }
                }
            }
            if (destino.length() == 0L) throw ArquivoRecusado(MENSAGEM_ARQUIVO_ILEGIVEL)
        } catch (e: ArquivoRecusado) {
            destino.delete()
            throw e
        } catch (_: Exception) {
            destino.delete()
            throw ArquivoRecusado(MENSAGEM_ARQUIVO_ILEGIVEL)
        }
        return DocumentoCopiado(nomeArquivo, formato, destino.length(), destino.absolutePath)
    }

    // ===== Fotos =====

    // JPG, PNG ou HEIC → JPEG com lado maior de até 1920 px e a orientação do EXIF aplicada, num
    // temporário. Câmera: "IMG_aaaaMMdd_HHmmss.jpg"; galeria: nome original com extensão ".jpg"
    fun prepararFoto(uri: Uri, daCamera: Boolean): FotoPreparada {
        val tipo = resolver.getType(uri)
        if (tipo != null && tipo !in TIPOS_FOTO) throw ArquivoRecusado(MENSAGEM_FOTO_INVALIDA)

        val nomeArquivo = if (daCamera) {
            "IMG_${LocalDateTime.now().format(FORMATO_NOME_CAMERA)}.jpg"
        } else {
            val original = consultarNomeETamanho(uri).first ?: "foto"
            "${original.substringBeforeLast('.')}.jpg"
        }

        val bitmap = try {
            semTransparencia(decodificar(uri))
        } catch (_: Exception) {
            throw ArquivoRecusado(MENSAGEM_FOTO_INVALIDA)
        } catch (_: OutOfMemoryError) {
            throw ArquivoRecusado(MENSAGEM_FOTO_INVALIDA)
        }

        val temporario = File(pasta(contexto.cacheDir, PASTA_FOTOS), "${UUID.randomUUID()}.jpg")
        try {
            temporario.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, it) }
        } catch (_: Exception) {
            temporario.delete()
            throw ArquivoRecusado(MENSAGEM_FOTO_INVALIDA)
        } finally {
            bitmap.recycle()
        }
        if (temporario.length() > Arquivo.TAMANHO_MAXIMO) {
            temporario.delete()
            throw ArquivoRecusado(MENSAGEM_TAMANHO_FOTO)
        }
        return FotoPreparada(temporario.absolutePath, nomeArquivo, temporario.length())
    }

    // Copia o temporário para files/fotos/ e devolve o caminho definitivo. O temporário só é apagado
    // depois de gravar no banco: se a gravação falhar, "Enviar foto" pode ser tocado de novo
    fun guardarFoto(foto: FotoPreparada): String {
        val destino = File(pasta(contexto.filesDir, PASTA_FOTOS), "${UUID.randomUUID()}.jpg")
        File(foto.caminhoTemporario).copyTo(destino, overwrite = true)
        return destino.absolutePath
    }

    // Caminho vazio (dados de exemplo) ou arquivo já ausente não é erro
    fun apagar(caminho: String) {
        if (caminho.isNotBlank()) File(caminho).delete()
    }

    // ===== Uris para outros apps =====

    // Arquivo novo para a câmera gravar; as capturas anteriores já foram processadas e são apagadas
    fun novaUriCaptura(): Uri {
        val pastaCamera = pasta(contexto.cacheDir, PASTA_CAMERA)
        pastaCamera.listFiles()?.forEach { it.delete() }
        val arquivo = File(pastaCamera, "captura_${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(contexto, autoridade, arquivo)
    }

    // null = o arquivo não existe neste aparelho (dados de exemplo, outro aparelho)
    fun uriCompartilhavel(caminho: String): Uri? {
        if (caminho.isBlank()) return null
        val arquivo = File(caminho)
        if (!arquivo.exists()) return null
        return FileProvider.getUriForFile(contexto, autoridade, arquivo)
    }

    // ===== Auxiliares =====

    private fun consultarNomeETamanho(uri: Uri): Pair<String?, Long?> =
        try {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use null to null
                    val colunaNome = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val colunaTamanho = cursor.getColumnIndex(OpenableColumns.SIZE)
                    val nome = if (colunaNome >= 0 && !cursor.isNull(colunaNome)) cursor.getString(colunaNome) else null
                    val tamanho = if (colunaTamanho >= 0 && !cursor.isNull(colunaTamanho)) cursor.getLong(colunaTamanho) else null
                    nome to tamanho
                } ?: (null to null)
        } catch (_: Exception) {
            null to null
        }

    // Android 9+: ImageDecoder já aplica o EXIF e lê HEIC. Antes: BitmapFactory + ExifInterface do
    // framework (HEIC não decodifica e vira "Não foi possível usar esta foto")
    private fun decodificar(uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val (largura, altura) = tamanhoLimitado(info.size.width, info.size.height)
                decoder.setTargetSize(largura, altura)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            decodificarLegado(uri)
        }

    private fun decodificarLegado(uri: Uri): Bitmap {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) throw ArquivoRecusado(MENSAGEM_FOTO_INVALIDA)

        val opcoes = BitmapFactory.Options().apply {
            inSampleSize = amostraParaReduzir(limites.outWidth, limites.outHeight, LADO_MAXIMO)
        }
        val lido = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) }
            ?: throw ArquivoRecusado(MENSAGEM_FOTO_INVALIDA)

        val graus = resolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val (largura, altura) = tamanhoLimitado(lido.width, lido.height)
        val escala = largura.toFloat() / lido.width
        if (graus == 0f && escala == 1f) return lido
        val matriz = Matrix().apply {
            postScale(escala, escala)
            postRotate(graus)
        }
        return Bitmap.createBitmap(lido, 0, 0, lido.width, lido.height, matriz, true).also {
            if (it !== lido) lido.recycle()
        }
    }

    // JPEG não tem canal alfa: um PNG com fundo transparente ficaria com fundo preto. Desenha sobre
    // branco antes de comprimir (foto sem transparência passa direto)
    private fun semTransparencia(bitmap: Bitmap): Bitmap {
        if (!bitmap.hasAlpha()) return bitmap
        val opaco = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(opaco).apply {
            drawColor(Color.WHITE)
            drawBitmap(bitmap, 0f, 0f, null)
        }
        bitmap.recycle()
        return opaco
    }

    // Lado maior de até LADO_MAXIMO, mantendo a proporção (nunca amplia)
    private fun tamanhoLimitado(largura: Int, altura: Int): Pair<Int, Int> {
        val maior = max(largura, altura)
        if (maior <= LADO_MAXIMO) return largura to altura
        val escala = LADO_MAXIMO.toFloat() / maior
        return (largura * escala).roundToInt().coerceAtLeast(1) to (altura * escala).roundToInt().coerceAtLeast(1)
    }

    private fun pasta(base: File, nome: String): File = File(base, nome).apply { mkdirs() }

    private companion object {
        const val PASTA_ARQUIVOS = "arquivos"
        const val PASTA_FOTOS = "fotos"
        const val PASTA_CAMERA = "camera"
        const val BUFFER = 64 * 1024
        const val LADO_MAXIMO = 1920
        const val QUALIDADE_JPEG = 85
        val TIPOS_FOTO = setOf("image/jpeg", "image/jpg", "image/png", "image/heic", "image/heif")
        val FORMATO_NOME_CAMERA: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

        const val MENSAGEM_FORMATO_DOCUMENTO = "Formato não aceito. Use PDF, DOCX, TXT ou XLSX."
        const val MENSAGEM_TAMANHO_DOCUMENTO = "O arquivo passa de 10 MB."
        const val MENSAGEM_ARQUIVO_ILEGIVEL = "Não foi possível ler este arquivo."
        const val MENSAGEM_FOTO_INVALIDA = "Não foi possível usar esta foto. Use JPG, PNG ou HEIC."
        const val MENSAGEM_TAMANHO_FOTO = "A foto passa de 10 MB."
    }
}
