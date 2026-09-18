package br.com.senac.linhasaereas.b2b;

/** Lançada quando um agente tenta acessar dados/reservas de empresa cliente diferente da selecionada (RN-E01; RNF-27). */
public class CrossTenantAccessException extends RuntimeException {
    public CrossTenantAccessException(String message) {
        super(message);
    }
}
