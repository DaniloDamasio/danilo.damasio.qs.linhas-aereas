package br.com.senac.linhasaereas.posvenda;

/**
 * Lançada quando uma alteração (remarcação/cancelamento) é efetivada sem que o custo/prazo
 * tenha sido previamente cotado e exibido ao usuário — RN-D02 (RF-17 exige exibição "antes da
 * confirmação da alteração").
 */
public class ChangeQuoteRequiredException extends RuntimeException {
    public ChangeQuoteRequiredException(String message) {
        super(message);
    }
}
