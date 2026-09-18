package br.com.senac.linhasaereas.estoque;

/** Lançada quando a escrita condicional (arquitetura.md §12.3) rejeita a tentativa. */
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
