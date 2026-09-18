package br.com.senac.linhasaereas.estoque;

/**
 * Meio de pagamento usado na sessão de HOLD, do qual depende o TTL aplicado (RN-A02, RN-A04;
 * decisão do stakeholder em 2026-09-17, plano-tdd.md Seção 3.4, item 1).
 */
public enum PaymentMethod {
    CARTAO,
    PIX
}
