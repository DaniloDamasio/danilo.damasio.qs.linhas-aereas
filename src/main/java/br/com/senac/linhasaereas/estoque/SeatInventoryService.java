package br.com.senac.linhasaereas.estoque;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço de Estoque de Assentos — único ponto de decisão de posse (arquitetura.md §9, §12).
 * A transição de estado de cada (voo, assento) é feita via {@link ConcurrentHashMap#compute},
 * que serializa o acesso por chave e serve como a "escrita condicional atômica" descrita em §12.3:
 * apenas uma tentativa concorrente consegue transicionar o estado; as demais são rejeitadas de
 * forma determinística, sem ambiguidade.
 */
public class SeatInventoryService {

    private final Map<String, Set<String>> mapaAssentosPorVoo;
    private final ConcurrentHashMap<String, SeatEntry> assentos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> holdIndex = new ConcurrentHashMap<>();
    private final AtomicLong holdSequence = new AtomicLong();

    public SeatInventoryService(Map<String, Set<String>> mapaAssentosPorVoo) {
        this.mapaAssentosPorVoo = mapaAssentosPorVoo;
    }

    public SeatHold hold(String vooId, String assentoId, String sessaoId, Duration ttl) {
        validarAssentoExiste(vooId, assentoId);
        String key = chave(vooId, assentoId);
        Instant expiresAt = Instant.now().plus(ttl);
        String novoHoldId = "hold-" + holdSequence.incrementAndGet();

        SeatEntry novo = assentos.compute(key, (k, atual) -> {
            if (atual != null && atual.status() == SeatStatus.CONFIRMED) {
                throw new SeatUnavailableException("assento já CONFIRMED: " + key);
            }
            if (atual != null && atual.status() == SeatStatus.HOLD && !expirou(atual.expiresAt())) {
                throw new SeatUnavailableException("assento indisponível (HOLD ativo): " + key);
            }
            return new SeatEntry(novoHoldId, vooId, assentoId, sessaoId, SeatStatus.HOLD, expiresAt);
        });
        holdIndex.put(novoHoldId, key);
        return novo.toSeatHold();
    }

    public SeatHold hold(String vooId, String assentoId, String sessaoId, PaymentMethod metodoPagamento) {
        Duration ttl = switch (metodoPagamento) {
            case CARTAO -> Duration.ofMinutes(3);
            case PIX -> Duration.ofMinutes(12);
        };
        return hold(vooId, assentoId, sessaoId, ttl);
    }

    public SeatHold confirm(String holdId) {
        String key = holdIndex.get(holdId);
        if (key == null) {
            throw new InvalidHoldException("HOLD inexistente: " + holdId);
        }
        SeatEntry confirmado = assentos.compute(key, (k, atual) -> {
            if (atual == null || !atual.holdId().equals(holdId) || atual.status() != SeatStatus.HOLD) {
                throw new InvalidHoldException("HOLD inválido ou não elegível para confirmação: " + holdId);
            }
            if (expirou(atual.expiresAt())) {
                throw new InvalidHoldException(
                        "HOLD expirado não pode ser confirmado sem revalidação: " + holdId);
            }
            return new SeatEntry(atual.holdId(), atual.vooId(), atual.assentoId(), atual.sessaoId(),
                    SeatStatus.CONFIRMED, atual.expiresAt());
        });
        return confirmado.toSeatHold();
    }

    public void cancel(String holdId) {
        String key = holdIndex.get(holdId);
        if (key == null) {
            throw new InvalidHoldException("HOLD inexistente: " + holdId);
        }
        assentos.compute(key, (k, atual) -> {
            if (atual == null || !atual.holdId().equals(holdId)) {
                throw new InvalidHoldException("HOLD inválido: " + holdId);
            }
            if (atual.status() == SeatStatus.CONFIRMED) {
                throw new InvalidHoldException("CONFIRMED é estado terminal, não pode ser cancelado: " + holdId);
            }
            return null;
        });
    }

    public SeatStatus statusOf(String vooId, String assentoId) {
        validarAssentoExiste(vooId, assentoId);
        SeatEntry atual = assentos.get(chave(vooId, assentoId));
        if (atual == null) {
            return SeatStatus.AVAILABLE;
        }
        if (atual.status() == SeatStatus.HOLD && expirou(atual.expiresAt())) {
            return SeatStatus.AVAILABLE;
        }
        return atual.status();
    }

    /**
     * Varredura ativa (sweeper, arquitetura.md §12.4): libera HOLDs vencidos sem depender de nova
     * tentativa sobre o assento. Pacote-privado — usado por {@link HoldSweeper}.
     */
    int sweepExpiredHolds() {
        List<Boolean> liberados = new ArrayList<>();
        for (String key : assentos.keySet()) {
            assentos.compute(key, (k, atual) -> {
                if (atual != null && atual.status() == SeatStatus.HOLD && expirou(atual.expiresAt())) {
                    liberados.add(Boolean.TRUE);
                    return null;
                }
                return atual;
            });
        }
        return liberados.size();
    }

    private void validarAssentoExiste(String vooId, String assentoId) {
        Set<String> assentosDoVoo = mapaAssentosPorVoo.get(vooId);
        if (assentosDoVoo == null || !assentosDoVoo.contains(assentoId)) {
            throw new SeatNotFoundException("assento " + assentoId + " não encontrado no voo " + vooId);
        }
    }

    private static boolean expirou(Instant expiresAt) {
        return Instant.now().isAfter(expiresAt);
    }

    private static String chave(String vooId, String assentoId) {
        return vooId + "::" + assentoId;
    }

    private record SeatEntry(
            String holdId, String vooId, String assentoId, String sessaoId, SeatStatus status, Instant expiresAt) {
        SeatHold toSeatHold() {
            return new SeatHold(holdId, vooId, assentoId, sessaoId, status, expiresAt);
        }
    }
}
