package br.com.senac.linhasaereas.posvenda;

import java.time.Duration;
import java.time.Instant;

/** Sessão de chat de suporte aberta pelo usuário — RN-D01 (RF-16; RNF-21). */
public record ChatSession(
        String sessionId,
        Instant abertaEm,
        Duration tempoAtePrimeiraResposta
) {
}
