package br.com.senac.linhasaereas.checkout;

import br.com.senac.linhasaereas.estoque.SeatHold;
import br.com.senac.linhasaereas.estoque.SeatInventoryService;

import br.com.senac.linhasaereas.estoque.SeatUnavailableException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Seleção de assentos com suporte a grupos/famílias — RN-C07 (RF-13).
 * Reutiliza o mecanismo de exclusividade de assento do Estoque (RN-A01/RN-A03) para cada assento do grupo.
 */
public class SeatGroupSelectionService {

    private final SeatInventoryService inventoryService;

    public SeatGroupSelectionService(SeatInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public List<SeatHold> selecionarGrupo(String vooId, List<String> assentosDesejados, String sessaoId, Duration ttl) {
        List<SeatHold> holdsConcedidos = new ArrayList<>();
        for (String assento : assentosDesejados) {
            try {
                holdsConcedidos.add(inventoryService.hold(vooId, assento, sessaoId, ttl));
            } catch (SeatUnavailableException e) {
                // Um assento do bloco tornou-se indisponível durante a seleção do grupo (concorrência de
                // terceiro, RN-A01/RN-A03). Libera o que já foi concedido para não deixar o grupo com uma
                // confirmação parcial silenciosa e informa a impossibilidade de completar o grupo.
                for (SeatHold hold : holdsConcedidos) {
                    inventoryService.cancel(hold.holdId());
                }
                throw new GrupoIncompletoException(
                        "não foi possível reservar todos os assentos do grupo para o voo " + vooId
                                + ": " + assento + " tornou-se indisponível");
            }
        }
        return holdsConcedidos;
    }
}
