package br.com.senac.linhasaereas.estoque;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-A01 — Estado ativo único por (voo, assento).
 * Origem: RESTRIÇÃO-CRÍTICA-01; RNF-03; RF-13; RF-26; arquitetura.md §12.1, §12.2.
 */
class RnA01EstadoAtivoUnicoPorAssentoTest {

    private static final String VOO = "VOO-100";
    private static final String ASSENTO = "12A";

    private SeatInventoryService novoServicoComAssento(String voo, String assento) {
        return new SeatInventoryService(Map.of(voo, Set.of(assento)));
    }

    @Test
    void rnA01_cf_reservaDeAssentoLivreConcedeHoldUnico() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);

        // Act
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMinutes(10));

        // Assert
        assertEquals(SeatStatus.HOLD, hold.status());
        assertEquals(SeatStatus.HOLD, service.statusOf(VOO, ASSENTO));
    }

    @Test
    void rnA01_cf_confirmacaoAposPagamentoAprovadoTransicionaHoldParaConfirmed() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        SeatHold hold = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMinutes(10));

        // Act
        SeatHold confirmado = service.confirm(hold.holdId());

        // Assert
        assertEquals(SeatStatus.CONFIRMED, confirmado.status());
        assertEquals(SeatStatus.CONFIRMED, service.statusOf(VOO, ASSENTO));
    }

    @Test
    void rnA01_conf_duasSolicitacoesDeHoldSimultaneasApenasUmaEAceita() throws InterruptedException {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger aceitas = new AtomicInteger(0);
        AtomicInteger rejeitadas = new AtomicInteger(0);

        Runnable tentativa = () -> {
            try {
                partida.await();
                service.hold(VOO, ASSENTO, Thread.currentThread().getName(), Duration.ofMinutes(10));
                aceitas.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (SeatUnavailableException e) {
                rejeitadas.incrementAndGet();
            }
        };

        // Act
        pool.submit(tentativa);
        pool.submit(tentativa);
        partida.countDown();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(1, aceitas.get(), "exatamente uma tentativa deve ser aceita");
        assertEquals(1, rejeitadas.get(), "exatamente uma tentativa deve ser rejeitada de forma determinística");
    }

    @Test
    void rnA01_proib_naoPodeConfirmarHoldParaAssentoJaConfirmado() {
        // Arrange: assento já vendido (CONFIRMED) para um passageiro
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        SeatHold primeiroHold = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMinutes(10));
        service.confirm(primeiroHold.holdId());

        // Act & Assert: nenhuma segunda venda pode ser confirmada para o mesmo assento
        assertThrows(SeatUnavailableException.class,
                () -> service.hold(VOO, ASSENTO, "sessao-2", Duration.ofMinutes(10)),
                "não deve ser possível iniciar novo HOLD sobre assento já CONFIRMED");
    }

    @Test
    void rnA01_inv_holdParaAssentoInexistenteNoVooERejeitado() {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);

        // Act & Assert
        assertThrows(SeatNotFoundException.class,
                () -> service.hold(VOO, "99Z", "sessao-1", Duration.ofMinutes(10)));
    }

    @Test
    void rnA01_lim_holdSobreAssentoComHoldConcorrenteJaExpiradoTrataComoAvailableEConcede() throws InterruptedException {
        // Arrange: HOLD com TTL curtíssimo, deixado expirar (expiração lazy, arquitetura.md §12.4)
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        SeatHold expirando = service.hold(VOO, ASSENTO, "sessao-1", Duration.ofMillis(1));
        Thread.sleep(50);

        // Act
        SeatHold novoHold = service.hold(VOO, ASSENTO, "sessao-2", Duration.ofMinutes(10));

        // Assert: comportamento determinístico único — trata como AVAILABLE e concede
        assertEquals(SeatStatus.HOLD, novoHold.status());
        assertNotEquals(expirando.holdId(), novoHold.holdId());
    }
}
