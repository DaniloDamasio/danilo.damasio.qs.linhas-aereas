package br.com.senac.linhasaereas.estoque;

import java.time.Instant;

/** Registro de posse (SeatHold/SeatSale) — arquitetura.md §12.1. */
public record SeatHold(
        String holdId,
        String vooId,
        String assentoId,
        String sessaoId,
        SeatStatus status,
        Instant expiresAt
) {
}
