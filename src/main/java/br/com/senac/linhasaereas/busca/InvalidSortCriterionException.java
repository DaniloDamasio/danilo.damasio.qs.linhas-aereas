package br.com.senac.linhasaereas.busca;

/** Lançada quando um critério de ordenação inexistente é solicitado — RN-B04. */
public class InvalidSortCriterionException extends RuntimeException {
    public InvalidSortCriterionException(String message) {
        super(message);
    }
}
