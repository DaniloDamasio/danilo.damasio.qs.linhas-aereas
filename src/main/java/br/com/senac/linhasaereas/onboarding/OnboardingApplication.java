package br.com.senac.linhasaereas.onboarding;

import java.time.Instant;

/** Solicitação de onboarding de uma companhia parceira — RF-29 (RN-G01). */
public record OnboardingApplication(
        String companhiaId,
        boolean documentacaoCompleta,
        boolean testesTecnicosAprovados,
        Instant iniciadoEm
) {
}
