package br.com.mateushb.extensionconnectapp.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.mateushb.extensionconnectapp.data.local.amostraParaReduzir
import br.com.mateushb.extensionconnectapp.ui.theme.GradientesFoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Foto do projeto (spec 006): grade do card Fotos (frame `35:231`), "Envios em andamento", prévia do
 * sheet de autorização (`35:630`) e sheet da foto. Mostra a imagem de [caminho] recortada para
 * preencher o espaço; enquanto carrega, ou se o arquivo não existe no aparelho (dados de exemplo), mostra
 * o gradiente do Figma escolhido por [semente] com o ícone de imagem branco no centro.
 *
 * A decodificação roda em `Dispatchers.IO` reduzida para [ladoMaximoPx] (sem biblioteca de imagens).
 * Com [proporcaoOriginal], a altura acompanha a proporção da foto carregada (sheet da foto); o
 * tamanho do placeholder vem de [modifier].
 */
@Composable
fun MiniaturaFoto(
    caminho: String,
    semente: Int,
    modifier: Modifier = Modifier,
    cantos: Dp = 8.dp,
    tamanhoIcone: Dp = 28.dp,
    ladoMaximoPx: Int = 360,
    proporcaoOriginal: Boolean = false
) {
    val imagem by produceState<ImageBitmap?>(initialValue = null, caminho, ladoMaximoPx) {
        value = withContext(Dispatchers.IO) { decodificarMiniatura(caminho, ladoMaximoPx) }
    }
    val (clara, escura) = GradientesFoto[Math.floorMod(semente, GradientesFoto.size)]
    val proporcao = imagem?.takeIf { proporcaoOriginal }?.let { it.width.toFloat() / it.height }

    Box(
        modifier = modifier
            .then(if (proporcao != null) Modifier.aspectRatio(proporcao) else Modifier)
            .clip(RoundedCornerShape(cantos))
            .background(
                Brush.linearGradient(
                    0f to clara,
                    0.5f to escura,
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val carregada = imagem
        if (carregada != null) {
            Image(
                bitmap = carregada,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(tamanhoIcone)
            )
        }
    }
}

// As fotos gravadas pelo app já estão na orientação certa (compressão em ArmazenamentoArquivos)
private fun decodificarMiniatura(caminho: String, ladoMaximoPx: Int): ImageBitmap? {
    if (caminho.isBlank()) return null
    val arquivo = File(caminho)
    if (!arquivo.exists()) return null
    return try {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(caminho, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
        val amostra = amostraParaReduzir(limites.outWidth, limites.outHeight, ladoMaximoPx)
        BitmapFactory.decodeFile(caminho, BitmapFactory.Options().apply { inSampleSize = amostra })?.asImageBitmap()
    } catch (_: Exception) {
        null
    } catch (_: OutOfMemoryError) {
        null
    }
}
