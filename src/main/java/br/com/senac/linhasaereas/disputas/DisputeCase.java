package br.com.senac.linhasaereas.disputas;

import java.time.Duration;
import java.time.Instant;

/** Caso de disputa de reembolso entre passageiro e companhia, em mediação (RN-G05). */
public record DisputeCase(
        String disputaId,
        String passageiroId,
        String companhiaId,
        DisputeStatus status,
        Instant abertaEm,
        boolean slaAcompanhado,
        Duration slaMaximo
) {
}
