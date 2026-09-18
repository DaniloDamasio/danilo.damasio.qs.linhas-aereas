package br.com.senac.linhasaereas.checkout;

/**
 * Composição da tarifa exibida antes da confirmação — RN-C08 (RF-14; RNF-15).
 * Classe de dados simples, sem regra de negócio.
 */
public record InformacaoTarifa(boolean bagagemDeMaoInclusa, boolean bagagemDespachadaInclusa) {
}
