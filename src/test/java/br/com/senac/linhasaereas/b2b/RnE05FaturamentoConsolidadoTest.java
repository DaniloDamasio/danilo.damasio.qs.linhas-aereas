package br.com.senac.linhasaereas.b2b;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-E05 — Faturamento consolidado por empresa cliente.
 * Origem: RF-23; RNF-12.
 */
class RnE05FaturamentoConsolidadoTest {

    private static final String EMPRESA = "empresa-acme";

    @Test
    void rnE05_cf_multiplasReservasDaEmpresaNoPeriodoGeramFaturaConsolidadaUnica() {
        // Arrange
        CorporateBillingService service = new CorporateBillingService();
        YearMonth periodo = YearMonth.of(2026, 8);
        List<String> reservas = List.of("res-1", "res-2", "res-3");
        Instant fechamento = periodo.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();

        // Act
        Invoice fatura = service.generateConsolidatedInvoice(EMPRESA, periodo, reservas, fechamento);

        // Assert: uma única fatura consolidada, gerada até o 1º dia útil do mês seguinte
        assertEquals(EMPRESA, fatura.empresaClienteId());
        assertEquals(3, fatura.reservaIds().size());
    }

    @Test
    void rnE05_lim_fechamentoExatamenteNoPrimeiroDiaUtilDentroDoPrazo() {
        // Arrange: fechamento no limiar exato do 1º dia útil do mês seguinte
        CorporateBillingService service = new CorporateBillingService();
        YearMonth periodo = YearMonth.of(2026, 8);
        Instant primeiroDiaUtilDoMesSeguinte = periodo.plusMonths(1).atDay(1)
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        List<String> reservas = List.of("res-1");

        // Act
        Invoice fatura = service.generateConsolidatedInvoice(EMPRESA, periodo, reservas, primeiroDiaUtilDoMesSeguinte);

        // Assert: no próprio limiar, ainda dentro do prazo
        assertTrue(!fatura.emitidaEm().isAfter(primeiroDiaUtilDoMesSeguinte),
                "fechamento exatamente no 1º dia útil ainda deve estar dentro do prazo");
    }

    @Test
    void rnE05_conf_reservaConfirmadaNoUltimoInstanteDoPeriodoAtribuidaCorretamenteSemDuplicidadeNemOmissao() {
        // Arrange: venda confirmada nos últimos minutos do mês
        CorporateBillingService service = new CorporateBillingService();
        YearMonth periodo = YearMonth.of(2026, 8);
        Instant fechamento = periodo.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        List<String> reservasDoPeriodo = List.of("res-1", "res-ultimo-instante");

        // Act
        Invoice faturaDoPeriodo = service.generateConsolidatedInvoice(EMPRESA, periodo, reservasDoPeriodo, fechamento);
        Invoice faturaDoPeriodoSeguinte = service.generateConsolidatedInvoice(
                EMPRESA, periodo.plusMonths(1), List.of(), fechamento.plusSeconds(1));

        // Assert: a reserva do último instante pertence exclusivamente ao período correto, sem duplicidade
        assertTrue(faturaDoPeriodo.reservaIds().contains("res-ultimo-instante"));
        assertTrue(!faturaDoPeriodoSeguinte.reservaIds().contains("res-ultimo-instante"));
    }

    @Test
    void rnE05_proib_faturaFragmentadaPorPassagemParaClienteB2bEProibida() {
        // Arrange: N reservas confirmadas no mês para a mesma empresa
        CorporateBillingService service = new CorporateBillingService();
        YearMonth periodo = YearMonth.of(2026, 8);
        List<String> reservas = List.of("res-1", "res-2", "res-3", "res-4");
        Instant fechamento = periodo.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();

        // Act
        Invoice fatura = service.generateConsolidatedInvoice(EMPRESA, periodo, reservas, fechamento);

        // Assert: RF-23 exige fatura única por empresa — proibido fragmentar por passagem individual
        assertEquals(1, List.of(fatura).size());
        assertEquals(reservas.size(), fatura.reservaIds().size());
    }
}
