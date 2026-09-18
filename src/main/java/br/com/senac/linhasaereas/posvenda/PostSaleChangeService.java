package br.com.senac.linhasaereas.posvenda;

/**
 * Remarcação e cancelamento self-service — RN-D02.
 * Origem: RF-17.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PostSaleChangeService {

    public ChangeQuote quoteRebooking(String reservationId, String newFlightId, String newSeatId) {
        throw new UnsupportedOperationException("cotação de remarcação ainda não implementada (RN-D02)");
    }

    public ChangeQuote quoteCancellation(String reservationId) {
        throw new UnsupportedOperationException("cotação de cancelamento ainda não implementada (RN-D02)");
    }

    public ChangeResult confirmChange(String quoteId) {
        throw new UnsupportedOperationException("confirmação de alteração ainda não implementada (RN-D02)");
    }
}
