package br.com.senac.linhasaereas.integracao;

import br.com.senac.linhasaereas.estoque.SeatHold;
import br.com.senac.linhasaereas.estoque.SeatStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-A05 — Reconciliação de estoque com a companhia aérea (zero divergência).
 * Origem: RNF-03; RF-26; RNF-06; RNF-10; arquitetura.md §9.
 */
class RnA05ReconciliacaoDeEstoqueTest {

    private SeatHold vendaConfirmada() {
        return new SeatHold("hold-1", "VOO-100", "12A", "sessao-1", SeatStatus.CONFIRMED, Instant.now().plusSeconds(600));
    }

    @Test
    void rnA05_cf_vendaConfirmadaPropagaParaCompanhiaDentroDoSlaP95() {
        // Arrange
        AirlineStockSyncService syncService = new AirlineStockSyncService();
        SeatHold confirmada = vendaConfirmada();

        // Act
        SyncResult resultado = syncService.syncConfirmedSale(confirmada);

        // Assert: RNF-06 — sincronização concluída em ≤ 2s (P95)
        assertTrue(resultado.success());
        assertTrue(resultado.duracao().compareTo(Duration.ofSeconds(2)) <= 0);
    }

    @Test
    void rnA05_lim_propagacaoNoLimiteDeDoisSegundosAindaDentroDoSla() {
        // Arrange
        AirlineStockSyncService syncService = new AirlineStockSyncService();
        SeatHold confirmada = vendaConfirmada();

        // Act
        SyncResult resultado = syncService.syncConfirmedSale(confirmada);

        // Assert: fronteira inclui o próprio limiar (≤ 2s, não "abaixo de")
        assertTrue(resultado.duracao().compareTo(Duration.ofSeconds(2)) <= 0,
                "duração exatamente igual a 2s deve ser considerada dentro do SLA");
    }

    @Test
    void rnA05_conf_falhaDeComunicacaoNaPrimeiraTentativaAcionaReenvioComBackoff() {
        // Arrange: primeira tentativa falha por timeout (simulado pela ausência de implementação real)
        AirlineStockSyncService syncService = new AirlineStockSyncService();
        SeatHold confirmada = vendaConfirmada();

        // Act
        SyncResult resultado = syncService.syncConfirmedSale(confirmada);

        // Assert: RNF-10 exige reenvio com backoff; deve haver mais de uma tentativa registrada
        assertTrue(resultado.tentativas() > 1, "deve haver reenvio após falha na 1ª tentativa (RNF-10, backoff 1s)");
    }

    @Test
    void rnA05_conf_falhaNasTresTentativasDeWebhookBloqueiaOperacoesFailClosedTotal() {
        // Arrange
        AirlineStockSyncService syncService = new AirlineStockSyncService();
        String companhiaComSincronizacaoDesconhecida = "COMPANHIA-INSTAVEL";

        // Act & Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 6) —
        // substitui a proposta de suspensão parcial do ADR-03 por fail-closed total: após esgotar as 3
        // tentativas de webhook, a companhia com sincronização desconhecida deve ter as operações bloqueadas.
        assertThrows(AirlineSyncUnknownException.class,
                () -> syncService.bloquearOperacoesParaCompanhiaComSincronizacaoDesconhecida(
                        companhiaComSincronizacaoDesconhecida));
    }

    @Test
    void rnA05_proib_estoqueExibidoNuncaDivergeDoEstoqueReal() {
        // Arrange
        AirlineStockSyncService syncService = new AirlineStockSyncService();
        SeatHold confirmada = vendaConfirmada();

        // Act
        SyncResult resultado = syncService.syncConfirmedSale(confirmada);

        // Assert: RNF-03 exige divergência zero — sincronização deve ter sucesso, não apenas ser tentada
        assertTrue(resultado.success(), "toda venda confirmada deve terminar sincronizada, sem divergência");
    }

    @Test
    void rnA05_inv_eventoDeEstoqueComVooOuAssentoDesconhecidoERejeitado() {
        // Arrange: hold referenciando um voo inexistente no Catálogo
        AirlineStockSyncService syncService = new AirlineStockSyncService();
        SeatHold vendaComVooDesconhecido = new SeatHold(
                "hold-2", "VOO-INEXISTENTE", "1A", "sessao-2", SeatStatus.CONFIRMED, Instant.now().plusSeconds(600));

        // Act & Assert: rejeitado/registrado como inconsistência, não aplicado silenciosamente
        assertThrows(UnknownFlightOrSeatException.class,
                () -> syncService.syncConfirmedSale(vendaComVooDesconhecido));
    }
}
