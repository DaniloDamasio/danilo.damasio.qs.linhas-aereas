package br.com.senac.linhasaereas.b2b;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

/**
 * Faturamento consolidado por empresa cliente (RN-E05).
 * Origem: RF-23; RNF-12.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class CorporateBillingService {

    public Invoice generateConsolidatedInvoice(String empresaClienteId, YearMonth periodo,
                                                List<String> reservaIdsDoPeriodo, Instant momentoDeFechamento) {
        throw new UnsupportedOperationException(
                "geração de fatura consolidada B2B ainda não implementada (RN-E05)");
    }
}
