package br.com.senac.linhasaereas.disputas;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-G05 — Mediação de disputas de reembolso com SLA.
 * Origem: RF-33. SLA contratual citado, mas valor não definido na fonte — lacuna L-03 fechada com
 * baseline pela Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 5):
 * 15 dias corridos, marcado como valor de stakeholder a validar futuramente com jurídico/contratos.
 */
class RnG05MediacaoDeDisputasDeReembolsoComSlaTest {

    @Test
    void rnG05_cf_aberturaDeDisputaEntrePassageiroECompanhiaIniciaFluxoDeMediacaoComAcompanhamentoDeSla() {
        // Arrange
        RefundDisputeMediationService service = new RefundDisputeMediationService();
        String passageiroId = "passageiro-1";
        String companhiaId = "COMPANHIA-01";
        String motivo = "reembolso não processado após cancelamento de voo";

        // Act
        DisputeCase disputa = service.abrirDisputa(passageiroId, companhiaId, motivo);

        // Assert
        assertEquals(DisputeStatus.ABERTA, disputa.status());
        assertTrue(disputa.slaAcompanhado(),
                "disputa registrada deve iniciar fluxo de mediação com acompanhamento de SLA");
    }

    @Test
    void rnG05_lim_slaDeMediacaoUsaBaselineDeQuinzeDiasCorridosConfirmadoPeloStakeholder() {
        // Arrange
        RefundDisputeMediationService service = new RefundDisputeMediationService();
        String passageiroId = "passageiro-2";
        String companhiaId = "COMPANHIA-02";
        String motivo = "reembolso não processado após cancelamento de voo";

        // Act
        DisputeCase disputa = service.abrirDisputa(passageiroId, companhiaId, motivo);

        // Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 5) —
        // o SLA de acompanhamento da disputa usa a baseline de 15 dias corridos (lacuna L-03 fechada).
        assertEquals(Duration.ofDays(15), disputa.slaMaximo(),
                "baseline do SLA de disputa de reembolso confirmada pelo stakeholder é de 15 dias corridos");
    }
}
