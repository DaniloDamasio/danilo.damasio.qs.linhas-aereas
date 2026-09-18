package br.com.senac.linhasaereas.b2b;

/** Lançada quando uma reserva viola a política de viagem da empresa cliente (RN-E02). */
public class PolicyViolationException extends RuntimeException {
    public PolicyViolationException(String message) {
        super(message);
    }
}
