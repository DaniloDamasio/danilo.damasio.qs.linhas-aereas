package br.com.senac.linhasaereas.posvenda;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Canal de suporte 24h — RN-D01.
 * Origem: RF-16; RNF-21.
 */
public class SupportChannel {

    public boolean isAvailable(Instant momento) {
        return true;
    }

    public ChatSession openChat(Instant momentoAcionamento) {
        return new ChatSession(UUID.randomUUID().toString(), momentoAcionamento, Duration.ZERO);
    }
}
