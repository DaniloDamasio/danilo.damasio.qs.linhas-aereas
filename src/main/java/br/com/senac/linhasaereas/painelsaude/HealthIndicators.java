package br.com.senac.linhasaereas.painelsaude;

/** Indicadores consolidados de saúde da operação por companhia (RN-G02). */
public record HealthIndicators(
        String companhiaId,
        long volumeFraude,
        long volumeDisputas,
        double slaMedioMinutos
) {
}
