package br.com.senac.linhasaereas.checkout;

/** Lançada quando não é possível reservar todos os assentos contíguos solicitados para o grupo (RN-C07). */
public class GrupoIncompletoException extends RuntimeException {
    public GrupoIncompletoException(String message) {
        super(message);
    }
}
