package br.com.senac.linhasaereas.seguranca;

/**
 * Trilha de auditoria de chamadas de API autenticadas (RNF-28, RN-H04).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class ApiCallAuditTrail {

    public AuditRecord registrarChamadaAutenticada(String chamadaId) {
        throw new UnsupportedOperationException("registro de auditoria de chamada de API ainda não implementado (RN-H04)");
    }
}
