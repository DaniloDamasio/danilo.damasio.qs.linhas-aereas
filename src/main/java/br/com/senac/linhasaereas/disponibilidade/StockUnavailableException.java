package br.com.senac.linhasaereas.disponibilidade;

/** Lançada quando o Serviço de Estoque está indisponível no ponto de decisão de posse (RN-I02). */
public class StockUnavailableException extends RuntimeException {
    public StockUnavailableException(String message) {
        super(message);
    }
}
