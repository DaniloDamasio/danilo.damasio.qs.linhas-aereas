package br.com.senac.linhasaereas.precificacao;

/**
 * Lançada quando o preço final no checkout diverge do preço exibido na busca, dentro da
 * mesma sessão, sem sinalização prévia ao usuário — RN-B03 (RF-03/RNF-01/RNF-14).
 */
public class PriceMismatchException extends RuntimeException {
    public PriceMismatchException(String message) {
        super(message);
    }
}
