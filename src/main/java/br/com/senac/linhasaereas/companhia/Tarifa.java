package br.com.senac.linhasaereas.companhia;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Tarifa publicada no catálogo da companhia parceira — RN-F01 (RF-24; RNF-31; RNF-16).
 */
public record Tarifa(
        String tarifaId,
        String rotaOrigem,
        String rotaDestino,
        LocalTime horario,
        String classe,
        BigDecimal valor,
        boolean publicada,
        Duration tempoAtePublicacao
) {
}
