package br.com.mateushb.extensionconnectapp.model

// Situação de um arquivo ou foto na fila de envio (spec 006). Sem backend, o envio é simulado pela
// FilaEnvio. No banco só há AGUARDANDO_ENVIO (pendente) e ENVIADO; ENVIANDO e o progresso são
// calculados na leitura (ArquivoRepository): com internet o pendente aparece "Enviando" (0% enquanto
// espera a vez), sem internet "Aguardando envio · sem internet"
enum class StatusEnvio {
    AGUARDANDO_ENVIO,
    ENVIANDO,
    ENVIADO
}
