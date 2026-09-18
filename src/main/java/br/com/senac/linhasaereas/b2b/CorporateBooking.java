package br.com.senac.linhasaereas.b2b;

/** Reserva feita por um agente B2B em nome de um funcionário de uma empresa cliente (RN-E01). */
public record CorporateBooking(
        String bookingId,
        String empresaClienteId,
        String funcionarioId,
        String vooId,
        String assentoId
) {
}
