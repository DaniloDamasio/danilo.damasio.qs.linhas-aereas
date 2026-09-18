package br.com.senac.linhasaereas.b2b;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

/** Fatura consolidada de uma empresa cliente B2B (RN-E05). */
public record Invoice(
        String invoiceId,
        String empresaClienteId,
        YearMonth periodo,
        List<String> reservaIds,
        Instant emitidaEm
) {
}
