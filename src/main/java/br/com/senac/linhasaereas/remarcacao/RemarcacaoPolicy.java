package br.com.senac.linhasaereas.remarcacao;

import java.math.BigDecimal;

/**
 * Política de remarcação/cancelamento de uma tarifa — RF-05 (RN-B05).
 * Dado simples, sem regra de negócio. {@code valorMulta} é {@code null} quando não há multa.
 */
public record RemarcacaoPolicy(String vooId, boolean semCustoDeRemarcacao, BigDecimal valorMulta) {
}
