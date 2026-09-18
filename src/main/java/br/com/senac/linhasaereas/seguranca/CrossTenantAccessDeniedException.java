package br.com.senac.linhasaereas.seguranca;

/** Lançada quando um tenant tenta acessar recursos de outro tenant (RNF-27, RN-H03). */
public class CrossTenantAccessDeniedException extends RuntimeException {
    public CrossTenantAccessDeniedException(String message) {
        super(message);
    }
}
