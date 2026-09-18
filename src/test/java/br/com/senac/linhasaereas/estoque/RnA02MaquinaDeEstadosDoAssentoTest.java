package br.com.senac.linhasaereas.estoque;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-A02 — Máquina de estados do assento (AVAILABLE → HOLD → CONFIRMED / HOLD → AVAILABLE).
 * Origem: arquitetura.md §12.2.
 */
class RnA02MaquinaDeEstadosDoAssentoTest {

    private static final String VOO = "VOO-100";
    private static final String ASSENTO = "12A";

    private SeatInventoryService novoServicoComAssento(String voo, String assento) {
        return new SeatInventoryService(Map.of(voo, Set.of(assento)));
    }

    @Test
    void rnA02_cf_cicloCompletoDeVendaAvailableHoldConfirmed() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        assertEquals(SeatStatus.AVAILABLE, service.statusOf(VOO, ASSENTO));

        // Act
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMinutes(10));
        SeatHold confirmado = service.confirm(hold.holdId());

        // Assert
        assertEquals(SeatStatus.HOLD, hold.status());
        assertEquals(SeatStatus.CONFIRMED, confirmado.status());
        assertEquals(SeatStatus.CONFIRMED, service.statusOf(VOO, ASSENTO));
    }

    @Test
    void rnA02_cf_cancelamentoVoluntarioDoHoldLiberaAssentoImediatamente() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMinutes(10));

        // Act
        service.cancel(hold.holdId());

        // Assert
        assertEquals(SeatStatus.AVAILABLE, service.statusOf(VOO, ASSENTO));
    }

    @Test
    void rnA02_lim_expiracaoExataNoTtlLiberaAssentoNaDisponibilidadeAgregada() throws InterruptedException {
        // Arrange: TTL mínimo controlável para observar a expiração no limiar
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMillis(1));

        // Act: aguarda o instante exato de expiração
        Thread.sleep(50);

        // Assert: assento reaparece como AVAILABLE (arquitetura.md §12.4)
        assertEquals(SeatStatus.AVAILABLE, service.statusOf(VOO, ASSENTO));
    }

    @Test
    void rnA02_proib_naoPodeReabrirOuReverterAssentoConfirmed() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMinutes(10));
        SeatHold confirmado = service.confirm(hold.holdId());

        // Act & Assert: CONFIRMED é estado terminal (§12.2), sem reversão automática
        assertThrows(InvalidHoldException.class, () -> service.cancel(confirmado.holdId()));
    }

    @Test
    void rnA02_inv_confirmarSemHoldPrevioERejeitado() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);

        // Act & Assert
        assertThrows(InvalidHoldException.class, () -> service.confirm("hold-inexistente"));
        assertEquals(SeatStatus.AVAILABLE, service.statusOf(VOO, ASSENTO));
    }
}
