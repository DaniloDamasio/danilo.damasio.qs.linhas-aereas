package br.com.senac.linhasaereas.deteccaofraude;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-G03 — Alertas automáticos de padrões anômalos (fraude).
 * Origem: RF-31; RNF-26.
 * Regra quantitativa definida na fonte: anomalia = volume de reembolsos ≥ 3× a média móvel dos últimos
 * 7 dias, para a mesma rota/companhia. Alerta deve disparar em ≤ 15 minutos após a detecção.
 */
class RnG03AlertasAutomaticosDePadroesAnomalosTest {

    private static final String ROTA = "GRU-JFK";

    @Test
    void rnG03_cf_volumeDeReembolsosAtingeTresVezesAMediaMovelDisparaAlertaDentroDoSla() {
        // Arrange
        RefundAnomalyDetectionService service = new RefundAnomalyDetectionService();
        double mediaMovel = 10.0;
        RefundVolumeSample amostra = new RefundVolumeSample(ROTA, 30.0, mediaMovel, Instant.now());

        // Act
        Optional<AnomalyAlert> alerta = service.avaliarAnomalia(amostra);

        // Assert
        assertTrue(alerta.isPresent(), "volume igual a 3x a média móvel deve disparar alerta");
        assertEquals(ROTA, alerta.get().rotaOuCompanhiaId());
    }

    @Test
    void rnG03_lim_volumeExatamenteIgualATresVezesAMediaDeveDispararNoLimiar() {
        // Arrange: fronteira exata do limiar (≥ 3× inclui o limite)
        RefundAnomalyDetectionService service = new RefundAnomalyDetectionService();
        double mediaMovel = 20.0;
        RefundVolumeSample amostra = new RefundVolumeSample(ROTA, mediaMovel * 3, mediaMovel, Instant.now());

        // Act
        Optional<AnomalyAlert> alerta = service.avaliarAnomalia(amostra);

        // Assert
        assertTrue(alerta.isPresent(), "volume exatamente igual a 3x a média deve disparar (limite incluído)");
    }

    @Test
    void rnG03_inv_volumeLigeiramenteAbaixoDoLimiarNaoDeveDispararAlerta() {
        // Arrange: volume = 2,9x a média móvel, abaixo do limiar de 3x
        RefundAnomalyDetectionService service = new RefundAnomalyDetectionService();
        double mediaMovel = 20.0;
        RefundVolumeSample amostra = new RefundVolumeSample(ROTA, mediaMovel * 2.9, mediaMovel, Instant.now());

        // Act
        Optional<AnomalyAlert> alerta = service.avaliarAnomalia(amostra);

        // Assert
        assertTrue(alerta.isEmpty(), "volume abaixo de 3x a média móvel não deve disparar alerta de anomalia");
    }

    @Test
    void rnG03_lim_alertaDisparadoExatamenteAosQuinzeMinutosAindaDentroDoSla() {
        // Arrange: detecção e disparo separados por exatamente 15 minutos
        RefundAnomalyDetectionService service = new RefundAnomalyDetectionService();
        Instant detectadoEm = Instant.parse("2026-09-17T10:00:00Z");
        Instant disparadoEm = Instant.parse("2026-09-17T10:15:00Z");

        // Act
        boolean dentroDoSla = service.dentroDoSlaDeQuinzeMinutos(detectadoEm, disparadoEm);

        // Assert
        assertTrue(dentroDoSla, "disparo exatamente aos 15 minutos ainda deve ser considerado dentro do SLA");
    }
}
