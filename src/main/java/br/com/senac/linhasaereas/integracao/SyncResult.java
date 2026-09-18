package br.com.senac.linhasaereas.integracao;

import java.time.Duration;

/** Resultado da sincronização de estoque confirmado com a companhia aérea — arquitetura.md §9 (RN-A05). */
public record SyncResult(boolean success, int tentativas, Duration duracao) {
}
