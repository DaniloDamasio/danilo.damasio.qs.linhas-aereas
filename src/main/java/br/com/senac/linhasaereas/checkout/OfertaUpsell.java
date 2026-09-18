package br.com.senac.linhasaereas.checkout;

/**
 * Oferta de add-on (upsell) apresentada no checkout — RN-C08 (RF-14; RNF-15).
 * Classe de dados simples, sem regra de negócio.
 */
public record OfertaUpsell(String nome, boolean preSelecionada) {
}
