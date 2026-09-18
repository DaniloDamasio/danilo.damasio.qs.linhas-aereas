package br.com.senac.linhasaereas.posvenda;

import java.time.Instant;

/**
 * Canal de suporte 24h — RN-D01.
 * Origem: RF-16; RNF-21.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class SupportChannel {

    public boolean isAvailable(Instant momento) {
        throw new UnsupportedOperationException("disponibilidade 24/7/365 do canal de suporte ainda não implementada (RN-D01)");
    }

    public ChatSession openChat(Instant momentoAcionamento) {
        throw new UnsupportedOperationException("abertura de chat de suporte ainda não implementada (RN-D01)");
    }
}
