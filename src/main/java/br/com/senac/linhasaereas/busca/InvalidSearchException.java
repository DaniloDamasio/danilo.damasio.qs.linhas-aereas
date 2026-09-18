package br.com.senac.linhasaereas.busca;

/** Lançada quando os critérios de busca são inválidos (ex.: origem = destino) — RN-B01. */
public class InvalidSearchException extends RuntimeException {
    public InvalidSearchException(String message) {
        super(message);
    }
}
