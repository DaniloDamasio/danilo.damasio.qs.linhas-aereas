package br.com.senac.linhasaereas.checkout;

import br.com.senac.linhasaereas.estoque.SeatHold;
import br.com.senac.linhasaereas.estoque.SeatInventoryService;

import java.time.Duration;
import java.util.List;

/**
 * Seleção de assentos com suporte a grupos/famílias — RN-C07 (RF-13).
 * Reutiliza o mecanismo de exclusividade de assento do Estoque (RN-A01/RN-A03) para cada assento do grupo.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class SeatGroupSelectionService {

    private final SeatInventoryService inventoryService;

    public SeatGroupSelectionService(SeatInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public List<SeatHold> selecionarGrupo(String vooId, List<String> assentosDesejados, String sessaoId, Duration ttl) {
        throw new UnsupportedOperationException("seleção de assentos em grupo ainda não implementada (RN-C07)");
    }
}
