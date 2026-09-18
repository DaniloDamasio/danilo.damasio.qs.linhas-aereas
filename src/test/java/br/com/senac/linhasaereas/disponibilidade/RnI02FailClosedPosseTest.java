package br.com.senac.linhasaereas.disponibilidade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-I02 — Comportamento fail-closed no ponto de decisão de posse.
 * Origem: arquitetura.md §0, §12; ADR-02 [proposta, não decisão].
 */
class RnI02FailClosedPosseTest {

    @Test
    void rnI02_proib_confirmarPosseComEstoqueIndisponivelDeveFalharDeFormaFechada() {
        // Arrange
        PossessionDecisionGateway gateway = new PossessionDecisionGateway();

        // Act & Assert: nunca conceder posse sem confirmação positiva do Estoque (RESTRIÇÃO-CRÍTICA-01)
        assertThrows(StockUnavailableException.class,
                () -> gateway.confirmarPosse("VOO-100", "12A", false),
                "com o Estoque indisponível, a decisão de posse deve falhar de forma fechada (fail-closed)");
    }

    @Test
    void rnI02_conf_buscaTambemFalhaDeFormaFechadaQuandoEstoqueIndisponivel() {
        // Arrange
        PossessionDecisionGateway gateway = new PossessionDecisionGateway();

        // Act & Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 6) —
        // substitui a proposta de fail-open controlado do ADR-02 por fail-closed total: a busca também deve
        // recusar quando o Estoque está indisponível, sem servir o último estado conhecido.
        assertThrows(StockUnavailableException.class,
                () -> gateway.buscarComFailClosed("VOO-100", false),
                "busca sob indisponibilidade do Estoque também deve falhar de forma fechada (fail-closed)");
    }
}
