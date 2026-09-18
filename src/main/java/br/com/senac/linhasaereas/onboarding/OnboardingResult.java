package br.com.senac.linhasaereas.onboarding;

/** Resultado do processo de onboarding de uma companhia parceira (RN-G01). */
public record OnboardingResult(
        String companhiaId,
        OnboardingStatus status,
        long duracaoDiasUteis
) {
}
