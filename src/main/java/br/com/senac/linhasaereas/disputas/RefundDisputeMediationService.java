package br.com.senac.linhasaereas.disputas;

/**
 * Mediação de disputas de reembolso com SLA — RF-33 (RN-G05).
 * SLA contratual citado na fonte, mas valor concreto não definido (lacuna L-03, Seção 3.2 do plano-tdd.md).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class RefundDisputeMediationService {

    private static final java.time.Duration SLA_BASELINE = java.time.Duration.ofDays(15);
    private final java.util.concurrent.atomic.AtomicLong sequencia = new java.util.concurrent.atomic.AtomicLong();

    public DisputeCase abrirDisputa(String passageiroId, String companhiaId, String motivo) {
        String disputaId = "disputa-" + sequencia.incrementAndGet();
        return new DisputeCase(
                disputaId, passageiroId, companhiaId, DisputeStatus.ABERTA,
                java.time.Instant.now(), true, SLA_BASELINE);
    }
}
