package br.com.senac.linhasaereas.posvenda;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Cotação de custo/prazo de uma alteração (remarcação/cancelamento) — RN-D02.
 * RF-17 exige exibição do custo/prazo antes da efetivação da alteração.
 */
public record ChangeQuote(
        String quoteId,
        String reservationId,
        ChangeType type,
        BigDecimal cost,
        Instant expiresAt
) {
}
