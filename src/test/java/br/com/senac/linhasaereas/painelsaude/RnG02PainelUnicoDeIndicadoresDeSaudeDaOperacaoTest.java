package br.com.senac.linhasaereas.painelsaude;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * RN-G02 — Painel único de indicadores de saúde da operação.
 * Origem: RF-30.
 */
class RnG02PainelUnicoDeIndicadoresDeSaudeDaOperacaoTest {

    @Test
    void rnG02_cf_consultaConsolidadaDeFraudeDisputasESlaPorCompanhiaExibeIndicadoresUnificados() {
        // Arrange
        OperationalHealthDashboardService service = new OperationalHealthDashboardService();
        String companhiaId = "COMPANHIA-01";

        // Act
        HealthIndicators indicadores = service.indicadoresConsolidados(companhiaId);

        // Assert
        assertEquals(companhiaId, indicadores.companhiaId());
    }

    @Test
    void rnG02_proib_vazamentoDeDadoDeUmaCompanhiaParaOPainelDeOutraNuncaPodeOcorrer() {
        // Arrange: painel administrativo consultado no contexto de uma companhia solicitante,
        // mas mirando indicadores de outra companhia (RNF-27)
        OperationalHealthDashboardService service = new OperationalHealthDashboardService();
        String companhiaSolicitante = "COMPANHIA-A";
        String companhiaAlvo = "COMPANHIA-B";

        // Act
        HealthIndicators indicadores = service.indicadoresNoContextoDe(companhiaSolicitante, companhiaAlvo);

        // Assert: o painel no contexto de A nunca pode expor dados pertencentes a B
        assertNotEquals(companhiaAlvo, indicadores.companhiaId(),
                "indicadores retornados no contexto da companhia solicitante não podem pertencer a outra companhia");
    }
}
