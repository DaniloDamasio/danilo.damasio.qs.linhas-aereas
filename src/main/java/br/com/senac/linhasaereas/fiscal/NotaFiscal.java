package br.com.senac.linhasaereas.fiscal;

import java.time.Duration;

/**
 * Nota fiscal/recibo emitido automaticamente — RN-C05 (RF-11; RNF-12).
 * Classe de dados simples, sem regra de negócio.
 */
public record NotaFiscal(
        String numero,
        boolean disponivelPorEmail,
        boolean disponivelParaDownload,
        Duration duracaoEmissao
) {
}
