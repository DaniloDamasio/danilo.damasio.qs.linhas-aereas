package br.com.senac.linhasaereas.onboarding;

/**
 * Onboarding padronizado de companhias parceiras — RF-29; RNF-29; ADR-05 [proposta, não decisão] (RN-G01).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PartnerAirlineOnboardingService {

    public OnboardingResult concluirOnboarding(OnboardingApplication solicitacao) {
        throw new UnsupportedOperationException(
                "onboarding padronizado de companhias parceiras ainda não implementado (RN-G01)");
    }

    public OnboardingResult avaliarNivelDeCapacidadeTecnica(OnboardingApplication solicitacao, boolean sincronizacaoEmTempoReal) {
        throw new UnsupportedOperationException(
                "avaliação de nível de capacidade técnica para onboarding ainda não implementada (RN-G01)");
    }
}
