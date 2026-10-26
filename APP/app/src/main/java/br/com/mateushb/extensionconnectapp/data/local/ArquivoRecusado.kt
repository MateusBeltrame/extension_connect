package br.com.mateushb.extensionconnectapp.data.local

// Lançada pelo ArmazenamentoArquivos quando o arquivo escolhido não pode ser usado (formato, tamanho,
// foto ilegível). A mensagem já é a do Snackbar (spec 006, HU-06 e HU-09)
class ArquivoRecusado(mensagem: String) : Exception(mensagem)
