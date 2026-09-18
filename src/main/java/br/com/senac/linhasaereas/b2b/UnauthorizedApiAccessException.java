package br.com.senac.linhasaereas.b2b;

/** Lançada quando uma chamada à API pública B2B não está autenticada ou usa token inválido/expirado (RN-E04). */
public class UnauthorizedApiAccessException extends RuntimeException {
    public UnauthorizedApiAccessException(String message) {
        super(message);
    }
}
