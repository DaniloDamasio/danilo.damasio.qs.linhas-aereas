package br.com.senac.linhasaereas.integracao;

/** Lançada quando um evento de sincronização referencia voo/assento desconhecido pela plataforma (RN-A05). */
public class UnknownFlightOrSeatException extends RuntimeException {
    public UnknownFlightOrSeatException(String message) {
        super(message);
    }
}
