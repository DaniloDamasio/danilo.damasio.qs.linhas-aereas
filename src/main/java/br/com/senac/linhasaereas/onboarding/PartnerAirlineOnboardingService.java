package br.com.senac.linhasaereas.onboarding;

/**
 * Onboarding padronizado de companhias parceiras — RF-29; RNF-29; ADR-05 [proposta, não decisão] (RN-G01).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PartnerAirlineOnboardingService {

    private static final long BASELINE_DIAS_UTEIS = 10;

    public OnboardingResult concluirOnboarding(OnboardingApplication solicitacao) {
        if (solicitacao.documentacaoCompleta() && solicitacao.testesTecnicosAprovados()) {
            return new OnboardingResult(solicitacao.companhiaId(), OnboardingStatus.CONCLUIDO, BASELINE_DIAS_UTEIS);
        }
        return new OnboardingResult(solicitacao.companhiaId(), OnboardingStatus.EM_ANALISE, 0);
    }

    public OnboardingResult avaliarNivelDeCapacidadeTecnica(OnboardingApplication solicitacao, boolean sincronizacaoEmTempoReal) {
        throw new UnsupportedOperationException(
                "avaliação de nível de capacidade técnica para onboarding ainda não implementada (RN-G01)");
    }
}
