package br.com.senac.linhasaereas.estoque;

/** Lançada quando uma operação referencia um HOLD inexistente ou não elegível para a transição pedida. */
public class InvalidHoldException extends RuntimeException {
    public InvalidHoldException(String message) {
        super(message);
    }
}
