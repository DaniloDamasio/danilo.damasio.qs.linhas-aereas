package br.com.senac.linhasaereas.disponibilidade;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-I01 — Disponibilidade de busca/checkout.
 * Origem: RNF-20.
 */
class RnI01DisponibilidadeBuscaTest {

    @Test
    void rnI01_cf_operacaoNormalAoLongoDoMesAtendeMetaDe99_9PorCento() {
        // Arrange
        SearchCheckoutAvailabilityMonitor monitor = new SearchCheckoutAvailabilityMonitor();

        // Act
        boolean atendeMeta = monitor.atendeMetaDeDisponibilidadeMensal(Duration.ZERO);

        // Assert
        assertTrue(atendeMeta, "operação normal, sem indisponibilidade, deve atender a meta de >=99,9%/mês");
    }

    @Test
    void rnI01_lim_indisponibilidadeDeExatamente43MinutosNoLimiteQualquerMinutoAlemViolaAMeta() {
        // Arrange
        SearchCheckoutAvailabilityMonitor monitor = new SearchCheckoutAvailabilityMonitor();

        // Act
        boolean noLimite = monitor.atendeMetaDeDisponibilidadeMensal(Duration.ofMinutes(43));
        boolean alemDoLimite = monitor.atendeMetaDeDisponibilidadeMensal(Duration.ofMinutes(44));

        // Assert
        assertTrue(noLimite, "43 minutos de indisponibilidade acumulada no mês ainda está no limite aceitável");
        assertFalse(alemDoLimite, "qualquer minuto além de 43 no mês viola a meta de disponibilidade");
    }
}
