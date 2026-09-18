package br.com.senac.linhasaereas.estoque;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço de Estoque de Assentos — único ponto de decisão de posse (arquitetura.md §9, §12).
 */
public class SeatInventoryService {

    private final Map<String, Set<String>> mapaAssentosPorVoo;
    private final Map<String, SeatHold> estadoPorChaveAssento = new ConcurrentHashMap<>();
    private final Map<String, String> chavePorHoldId = new ConcurrentHashMap<>();

    public SeatInventoryService(Map<String, Set<String>> mapaAssentosPorVoo) {
        this.mapaAssentosPorVoo = mapaAssentosPorVoo;
    }

    private static String chave(String vooId, String assentoId) {
        return vooId + "::" + assentoId;
    }

    private void validarAssentoExiste(String vooId, String assentoId) {
        Set<String> assentos = mapaAssentosPorVoo.get(vooId);
        if (assentos == null || !assentos.contains(assentoId)) {
            throw new SeatNotFoundException("assento " + assentoId + " não existe no mapa do voo " + vooId);
        }
    }

    /** Trata HOLD expirado (lazy expiration, arquitetura.md §12.4) como AVAILABLE. */
    private boolean estaLivre(SeatHold atual) {
        if (atual == null) {
            return true;
        }
        if (atual.status() == SeatStatus.HOLD && Instant.now().isAfter(atual.expiresAt())) {
            return true;
        }
        return false;
    }

    public SeatHold hold(String vooId, String assentoId, String sessaoId, Duration ttl) {
        validarAssentoExiste(vooId, assentoId);
        String chave = chave(vooId, assentoId);
        SeatHold novoHold = new SeatHold(
                UUID.randomUUID().toString(), vooId, assentoId, sessaoId, SeatStatus.HOLD, Instant.now().plus(ttl));

        while (true) {
            SeatHold atual = estadoPorChaveAssento.get(chave);
            if (!estaLivre(atual)) {
                throw new SeatUnavailableException(
                        "assento " + assentoId + " do voo " + vooId + " não está disponível para HOLD");
            }
            boolean sucesso;
            if (atual == null) {
                sucesso = estadoPorChaveAssento.putIfAbsent(chave, novoHold) == null;
            } else {
                sucesso = estadoPorChaveAssento.replace(chave, atual, novoHold);
            }
            if (sucesso) {
                chavePorHoldId.put(novoHold.holdId(), chave);
                return novoHold;
            }
            // outra thread venceu a corrida ou alterou o estado entre a leitura e a escrita; reavalia
        }
    }

    public SeatHold hold(String vooId, String assentoId, String sessaoId, PaymentMethod metodoPagamento) {
        Duration ttl = metodoPagamento == PaymentMethod.PIX ? Duration.ofMinutes(15) : Duration.ofMinutes(30);
        return hold(vooId, assentoId, sessaoId, ttl);
    }

    public SeatHold confirm(String holdId) {
        String chave = chavePorHoldId.get(holdId);
        if (chave == null) {
            throw new InvalidHoldException("HOLD " + holdId + " não encontrado");
        }
        while (true) {
            SeatHold atual = estadoPorChaveAssento.get(chave);
            if (atual == null || !atual.holdId().equals(holdId) || atual.status() != SeatStatus.HOLD
                    || Instant.now().isAfter(atual.expiresAt())) {
                throw new InvalidHoldException("HOLD " + holdId + " não está elegível para confirmação");
            }
            SeatHold confirmado = new SeatHold(
                    atual.holdId(), atual.vooId(), atual.assentoId(), atual.sessaoId(),
                    SeatStatus.CONFIRMED, atual.expiresAt());
            if (estadoPorChaveAssento.replace(chave, atual, confirmado)) {
                return confirmado;
            }
        }
    }

    public void cancel(String holdId) {
        String chave = chavePorHoldId.get(holdId);
        if (chave == null) {
            throw new InvalidHoldException("HOLD " + holdId + " não encontrado");
        }
        SeatHold atual = estadoPorChaveAssento.get(chave);
        if (atual == null || !atual.holdId().equals(holdId) || atual.status() != SeatStatus.HOLD) {
            // CONFIRMED é estado terminal (arquitetura.md §12.2) — sem reversão automática.
            throw new InvalidHoldException("HOLD " + holdId + " não está em estado cancelável (HOLD)");
        }
        estadoPorChaveAssento.remove(chave, atual);
    }

    public SeatStatus statusOf(String vooId, String assentoId) {
        validarAssentoExiste(vooId, assentoId);
        SeatHold atual = estadoPorChaveAssento.get(chave(vooId, assentoId));
        if (estaLivre(atual)) {
            return SeatStatus.AVAILABLE;
        }
        return atual.status();
    }
}
