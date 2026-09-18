package br.com.senac.linhasaereas.estoque;

/** Lançada quando o assento não existe no mapa da aeronave do voo. */
public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException(String message) {
        super(message);
    }
}
