package br.com.senac.linhasaereas.deteccaofraude;

import java.time.Instant;

/** Amostra de volume de reembolsos observada para uma rota/companhia (RN-G03). */
public record RefundVolumeSample(
        String rotaOuCompanhiaId,
        double volumeReembolsos,
        double mediaMovelSeteDias,
        Instant observadoEm
) {
}
