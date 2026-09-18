package br.com.senac.linhasaereas.seguranca;

/**
 * Trilha de auditoria de chamadas de API autenticadas (RNF-28, RN-H04).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class ApiCallAuditTrail {

    private static final java.time.Duration RETENCAO_MINIMA = java.time.Duration.ofDays(5 * 365);

    public AuditRecord registrarChamadaAutenticada(String chamadaId) {
        return new AuditRecord(chamadaId, java.time.Instant.now(), RETENCAO_MINIMA);
    }
}
