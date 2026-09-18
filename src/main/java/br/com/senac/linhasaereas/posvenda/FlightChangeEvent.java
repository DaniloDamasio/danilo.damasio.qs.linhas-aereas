package br.com.senac.linhasaereas.posvenda;

import java.time.Instant;

/** Evento de alteração/cancelamento de voo recebido do Adaptador de Integração — RN-D03 (RF-18). */
public record FlightChangeEvent(
        String flightId,
        FlightEventType type,
        Instant occurredAt
) {
}
