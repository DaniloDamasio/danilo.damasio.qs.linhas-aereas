package br.com.senac.linhasaereas.remarcacao;

/**
 * Lançada quando a compra é concluída sem que a política de remarcação/cancelamento
 * tenha sido exibida ao usuário antes da compra — RN-B05 (RF-05).
 */
public class PolicyNotDisplayedException extends RuntimeException {
    public PolicyNotDisplayedException(String message) {
        super(message);
    }
}
