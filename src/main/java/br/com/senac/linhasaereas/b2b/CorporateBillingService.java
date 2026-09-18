package br.com.senac.linhasaereas.b2b;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * Faturamento consolidado por empresa cliente (RN-E05).
 * Origem: RF-23; RNF-12.
 */
public class CorporateBillingService {

    public Invoice generateConsolidatedInvoice(String empresaClienteId, YearMonth periodo,
                                                List<String> reservaIdsDoPeriodo, Instant momentoDeFechamento) {
        return new Invoice(UUID.randomUUID().toString(), empresaClienteId, periodo,
                List.copyOf(reservaIdsDoPeriodo), momentoDeFechamento);
    }
}
