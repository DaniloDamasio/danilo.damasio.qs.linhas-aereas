package br.com.senac.linhasaereas.disputas;

/**
 * Mediação de disputas de reembolso com SLA — RF-33 (RN-G05).
 * SLA contratual citado na fonte, mas valor concreto não definido (lacuna L-03, Seção 3.2 do plano-tdd.md).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class RefundDisputeMediationService {

    public DisputeCase abrirDisputa(String passageiroId, String companhiaId, String motivo) {
        throw new UnsupportedOperationException(
                "abertura de disputa de reembolso com acompanhamento de SLA ainda não implementada (RN-G05)");
    }
}
