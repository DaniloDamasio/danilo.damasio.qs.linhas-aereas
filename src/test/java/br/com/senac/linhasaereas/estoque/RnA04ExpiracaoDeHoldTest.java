package br.com.senac.linhasaereas.estoque;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-A04 — Expiração de HOLD (lazy + sweeper).
 * Origem: arquitetura.md §12.4. TTL não definido na fonte — lacuna L-07 (plano §3.2).
 */
class RnA04ExpiracaoDeHoldTest {

    private static final String VOO = "VOO-100";
    private static final String ASSENTO = "12A";

    private SeatInventoryService novoServicoComAssento(String voo, String assento) {
        return new SeatInventoryService(Map.of(voo, Set.of(assento)));
    }

    @Test
    void rnA04_cf_sweeperPeriodicoLiberaHoldVencidoSemNovaTentativaSobreOAssento() throws InterruptedException {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMillis(1));
        Thread.sleep(50);
        HoldSweeper sweeper = new HoldSweeper(service);

        // Act: nenhuma nova tentativa sobre o assento; apenas a varredura periódica
        int liberados = sweeper.runSweep();

        // Assert
        assertEquals(1, liberados);
        assertEquals(SeatStatus.AVAILABLE, service.statusOf(VOO, ASSENTO));
    }

    @Test
    void rnA04_cf_expiracaoLazyDetectadaEmNovaTentativaDeHold() throws InterruptedException {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        SeatHold expirando = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMillis(1));
        Thread.sleep(50);

        // Act: nova tentativa de HOLD sobre o mesmo assento, sem sweeper ter rodado
        SeatHold novoHold = service.hold(VOO, ASSENTO, "sessao-2", Duration.ofMinutes(10));

        // Assert: assento tratado como AVAILABLE; nova tentativa concedida
        assertEquals(SeatStatus.HOLD, novoHold.status());
        assertNotEquals(expirando.holdId(), novoHold.holdId());
    }

    @Test
    void rnA04_lim_ttlDoHoldEDiferenciadoPorMeioDePagamentoCartaoEntreDoisECincoMinutos() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);

        // Act
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", PaymentMethod.CARTAO);
        Duration ttlConcedido = Duration.between(Instant.now(), hold.expiresAt());

        // Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 1) —
        // TTL do HOLD por cartão de crédito/débito deve estar entre 2 e 5 minutos (lacuna L-07 fechada).
        assertTrue(ttlConcedido.compareTo(Duration.ofMinutes(2)) >= 0
                        && ttlConcedido.compareTo(Duration.ofMinutes(5)) <= 0,
                "TTL do HOLD por cartão deve estar entre 2 e 5 minutos");
    }

    @Test
    void rnA04_lim_ttlDoHoldEDiferenciadoPorMeioDePagamentoPixEntreDezEQuinzeMinutos() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);

        // Act
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", PaymentMethod.PIX);
        Duration ttlConcedido = Duration.between(Instant.now(), hold.expiresAt());

        // Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 1) —
        // TTL do HOLD via Pix deve estar entre 10 e 15 minutos (lacuna L-07 fechada).
        assertTrue(ttlConcedido.compareTo(Duration.ofMinutes(10)) >= 0
                        && ttlConcedido.compareTo(Duration.ofMinutes(15)) <= 0,
                "TTL do HOLD via Pix deve estar entre 10 e 15 minutos");
    }

    @Test
    void rnA04_proib_confirmacaoDeHoldExpiradoSemRevalidacaoDeveSerImpedida() throws InterruptedException {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMillis(1));
        Thread.sleep(50);

        // Act & Assert: CONFIRM não pode aceitar um HOLD cujo TTL já expirou
        assertThrows(InvalidHoldException.class, () -> service.confirm(hold.holdId()));
    }
}
