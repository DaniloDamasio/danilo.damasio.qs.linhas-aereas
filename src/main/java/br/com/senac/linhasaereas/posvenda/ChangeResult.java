package br.com.senac.linhasaereas.posvenda;

import java.math.BigDecimal;
import java.time.Instant;

/** Resultado de uma alteração self-service efetivada (remarcação/cancelamento) — RN-D02. */
public record ChangeResult(
        String reservationId,
        ChangeType type,
        BigDecimal costCharged,
        Instant effectiveAt
) {
}
