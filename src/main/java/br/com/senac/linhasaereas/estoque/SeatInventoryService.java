package br.com.senac.linhasaereas.estoque;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * Serviço de Estoque de Assentos — único ponto de decisão de posse (arquitetura.md §9, §12).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class SeatInventoryService {

    private final Map<String, Set<String>> mapaAssentosPorVoo;

    public SeatInventoryService(Map<String, Set<String>> mapaAssentosPorVoo) {
        this.mapaAssentosPorVoo = mapaAssentosPorVoo;
    }

    public SeatHold hold(String vooId, String assentoId, String sessaoId, Duration ttl) {
        throw new UnsupportedOperationException("HOLD ainda não implementado (RN-A01/RN-A02/RN-A03/RN-A04)");
    }

    public SeatHold hold(String vooId, String assentoId, String sessaoId, PaymentMethod metodoPagamento) {
        throw new UnsupportedOperationException(
                "HOLD com TTL diferenciado por meio de pagamento ainda não implementado (RN-A02/RN-A04)");
    }

    public SeatHold confirm(String holdId) {
        throw new UnsupportedOperationException("CONFIRM ainda não implementado (RN-A01/RN-A02)");
    }

    public void cancel(String holdId) {
        throw new UnsupportedOperationException("cancelamento de HOLD ainda não implementado (RN-A02)");
    }

    public SeatStatus statusOf(String vooId, String assentoId) {
        throw new UnsupportedOperationException("consulta de status ainda não implementada");
    }
}
