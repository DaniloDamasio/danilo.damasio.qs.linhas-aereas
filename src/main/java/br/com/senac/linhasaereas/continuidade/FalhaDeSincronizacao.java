package br.com.senac.linhasaereas.continuidade;

import java.time.Instant;

/**
 * Representa uma falha prolongada de sincronização de estoque com a companhia parceira —
 * RN-F03 (RF-26; RNF-06; RNF-10; RNF-22).
 */
public record FalhaDeSincronizacao(
        String companhiaId,
        Instant inicioFalha,
        Instant fimFalha
) {
}
