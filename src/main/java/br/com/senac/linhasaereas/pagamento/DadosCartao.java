package br.com.senac.linhasaereas.pagamento;

/**
 * Dados de cartão informados no checkout — RN-C03 (RF-09; RNF-24).
 * Classe de dados simples, sem regra de negócio. O número (PAN) é mantido apenas em memória
 * transitória neste objeto de entrada; a proibição de armazenamento em texto claro (RNF-24)
 * recai sobre o que o serviço de pagamento persiste, não sobre este objeto de transporte.
 */
public record DadosCartao(String numeroCartao, String validade, String cvv) {
}
