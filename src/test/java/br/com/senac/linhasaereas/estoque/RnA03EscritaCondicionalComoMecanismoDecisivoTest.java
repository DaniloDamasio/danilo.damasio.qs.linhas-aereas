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

/**
 * RN-A03 — Escrita condicional (CAS) como mecanismo decisivo de posse.
 * Origem: arquitetura.md §12.3; RESTRIÇÃO-CRÍTICA-01.
 */
class RnA03EscritaCondicionalComoMecanismoDecisivoTest {

    private static final String VOO = "VOO-100";
    private static final String ASSENTO = "12A";

    private SeatInventoryService novoServicoComAssento(String voo, String assento) {
        return new SeatInventoryService(Map.of(voo, Set.of(assento)));
    }

    @Test
    void rnA03_cf_duasLeiturasConcorrentesDoMesmoAvailableApenasUmaEscritaVencedora() throws InterruptedException {
        // Arrange
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger aceitas = new AtomicInteger(0);
        AtomicInteger rejeitadasDeterministicas = new AtomicInteger(0);

        Runnable tentativaA = () -> {
            try {
                partida.await();
                service.hold(VOO, ASSENTO, "sessao-A", Duration.ofMinutes(10));
                aceitas.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (SeatUnavailableException e) {
                rejeitadasDeterministicas.incrementAndGet();
            }
        };
        Runnable tentativaB = () -> {
            try {
                partida.await();
                service.hold(VOO, ASSENTO, "sessao-B", Duration.ofMinutes(10));
                aceitas.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (SeatUnavailableException e) {
                rejeitadasDeterministicas.incrementAndGet();
            }
        };

        // Act
        pool.submit(tentativaA);
        pool.submit(tentativaB);
        partida.countDown();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // Assert: apenas quem "commitar" primeiro é aceito; a outra é rejeição determinística
        assertEquals(2, aceitas.get());
        assertEquals(1, rejeitadasDeterministicas.get());
    }

    @Test
    void rnA03_conf_nTentativasConcorrentesApenasUmaAceitaTodasAsDemaisRejeitadas() throws InterruptedException {
        // Arrange
        int n = 10;
        SeatInventoryService service = novoServicoComAssento(VOO, ASSENTO);
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger aceitas = new AtomicInteger(0);
        AtomicInteger rejeitadas = new AtomicInteger(0);

        // Act
        for (int i = 0; i < n; i++) {
            String sessaoId = "sessao-" + i;
            pool.submit(() -> {
                try {
                    partida.await();
                    service.hold(VOO, ASSENTO, sessaoId, Duration.ofMinutes(10));
                    aceitas.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (SeatUnavailableException e) {
                    rejeitadas.incrementAndGet();
                }
            });
        }
        partida.countDown();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // Assert: exatamente uma aceita, N-1 rejeitadas
        assertEquals(1, aceitas.get());
        assertEquals(n - 1, rejeitadas.get());
    }
}
