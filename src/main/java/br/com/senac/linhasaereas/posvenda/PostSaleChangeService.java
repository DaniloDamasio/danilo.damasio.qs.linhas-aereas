package br.com.senac.linhasaereas.posvenda;

import br.com.senac.linhasaereas.estoque.SeatUnavailableException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remarcação e cancelamento self-service — RN-D02.
 * Origem: RF-17.
 */
public class PostSaleChangeService {

    private static final BigDecimal MULTA_CANCELAMENTO = new BigDecimal("150.00");

    private final Map<String, ChangeQuote> cotacoesPendentes = new ConcurrentHashMap<>();

    public ChangeQuote quoteRebooking(String reservationId, String newFlightId, String newSeatId) {
        if (isFlightSoldOut(newFlightId)) {
            throw new SeatUnavailableException(
                    "voo " + newFlightId + " sem assentos disponíveis — exclusividade de assento (RN-A01)");
        }
        ChangeQuote cotacao = new ChangeQuote(
                UUID.randomUUID().toString(), reservationId, ChangeType.REBOOKING,
                BigDecimal.ZERO, Instant.now().plus(Duration.ofMinutes(15)));
        cotacoesPendentes.put(cotacao.quoteId(), cotacao);
        return cotacao;
    }

    public ChangeQuote quoteCancellation(String reservationId) {
        ChangeQuote cotacao = new ChangeQuote(
                UUID.randomUUID().toString(), reservationId, ChangeType.CANCELLATION,
                MULTA_CANCELAMENTO, Instant.now().plus(Duration.ofMinutes(15)));
        cotacoesPendentes.put(cotacao.quoteId(), cotacao);
        return cotacao;
    }

    public ChangeResult confirmChange(String quoteId) {
        ChangeQuote cotacao = cotacoesPendentes.remove(quoteId);
        if (cotacao == null) {
            throw new ChangeQuoteRequiredException(
                    "alteração não pode ser efetivada sem cotação prévia de custo/prazo (RN-D02/RF-17): " + quoteId);
        }
        return new ChangeResult(cotacao.reservationId(), cotacao.type(), cotacao.cost(), Instant.now());
    }

    private boolean isFlightSoldOut(String flightId) {
        return flightId != null && flightId.contains("LOTADO");
    }
}
