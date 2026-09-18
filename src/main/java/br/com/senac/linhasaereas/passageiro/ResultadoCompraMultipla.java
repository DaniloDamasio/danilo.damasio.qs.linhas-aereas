package br.com.senac.linhasaereas.passageiro;

/**
 * Resultado estrutural de uma compra com múltiplos passageiros — RN-C06 (RF-12; RNF-17).
 * Classe de dados simples, sem regra de negócio.
 */
public record ResultadoCompraMultipla(int totalPassageiros, double percentualCamposReaproveitados) {
}
