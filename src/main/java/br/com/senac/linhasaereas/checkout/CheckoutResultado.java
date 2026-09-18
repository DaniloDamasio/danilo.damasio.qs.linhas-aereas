package br.com.senac.linhasaereas.checkout;

import java.time.Duration;

/**
 * Resultado estrutural de um fluxo de checkout — RN-C01 (RF-07; RNF-05; RNF-13).
 * Classe de dados simples, sem regra de negócio.
 */
public record CheckoutResultado(
        int telas,
        int cliques,
        int camposObrigatoriosAdicionais,
        Duration duracaoTotal
) {
}
