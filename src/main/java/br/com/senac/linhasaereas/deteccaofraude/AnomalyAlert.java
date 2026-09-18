package br.com.senac.linhasaereas.deteccaofraude;

import java.time.Instant;

/** Alerta automático disparado ao detectar padrão anômalo de reembolsos (RN-G03). */
public record AnomalyAlert(
        String rotaOuCompanhiaId,
        double volumeObservado,
        double limiar,
        Instant disparadoEm
) {
}
