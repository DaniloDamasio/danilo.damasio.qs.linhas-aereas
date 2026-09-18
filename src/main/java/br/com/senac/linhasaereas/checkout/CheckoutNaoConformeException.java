package br.com.senac.linhasaereas.checkout;

/** Lançada quando o fluxo de checkout excede os limites estruturais da RNF-13 (RN-C01). */
public class CheckoutNaoConformeException extends RuntimeException {
    public CheckoutNaoConformeException(String message) {
        super(message);
    }
}
