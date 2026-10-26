package br.com.mateushb.extensionconnectapp.data.local

import kotlin.math.max

// inSampleSize do BitmapFactory para ler uma imagem já reduzida: a maior potência de 2 que ainda deixa
// o lado maior em pelo menos [ladoMaximo] (a redução fina vem depois). Usado na compressão da foto
// (ArmazenamentoArquivos) e nas miniaturas (MiniaturaFoto)
internal fun amostraParaReduzir(largura: Int, altura: Int, ladoMaximo: Int): Int {
    var amostra = 1
    while (max(largura, altura) / (amostra * 2) >= ladoMaximo) amostra *= 2
    return amostra
}
