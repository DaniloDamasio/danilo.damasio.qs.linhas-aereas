package br.com.senac.linhasaereas.pagamento;

/**
 * Resultado de uma transação de pagamento — RN-C03 (RF-09; RNF-24).
 * Classe de dados simples, sem regra de negócio.
 */
public record ResultadoPagamento(String transacaoId, StatusPagamento status) {
}
