package br.com.senac.linhasaereas.pagamento;

/** Lançada quando o cartão informado é inválido ou a operadora recusa a transação (RN-C03). */
public class CartaoRecusadoException extends RuntimeException {
    public CartaoRecusadoException(String message) {
        super(message);
    }
}
