package br.com.senac.linhasaereas.lote;

import br.com.senac.linhasaereas.estoque.SeatHold;
import br.com.senac.linhasaereas.estoque.SeatInventoryService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Emissão em lote a partir de planilha — arquitetura.md §9, §11.3, §12.6 (RN-A06/RN-E03).
 * Cada item reusa o mesmo mecanismo de exclusividade de assento do Estoque (RN-A01/RN-A03):
 * uma tentativa de HOLD + CONFIRM por passageiro, sem forçar venda em caso de indisponibilidade.
 */
public class BatchIssuanceService {

    private static final int LIMITE_MAXIMO_PASSAGEIROS = 50;
    private static final Duration TTL_PADRAO_DO_ITEM_DE_LOTE = Duration.ofMinutes(10);

    private final SeatInventoryService inventoryService;

    public BatchIssuanceService(SeatInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public BatchResult processBatch(List<PassengerSeatRequest> requisicoes) {
        if (requisicoes.size() > LIMITE_MAXIMO_PASSAGEIROS) {
            throw new BatchSizeExceededException(
                    "lote com " + requisicoes.size() + " passageiros excede o limite de "
                            + LIMITE_MAXIMO_PASSAGEIROS + " — rejeitado inteiramente na ingestão, "
                            + "sem processamento parcial");
        }
        List<BatchItemResult> itens = new ArrayList<>();
        for (PassengerSeatRequest requisicao : requisicoes) {
            itens.add(processarItem(requisicao));
        }
        return new BatchResult(itens);
    }

    private BatchItemResult processarItem(PassengerSeatRequest requisicao) {
        try {
            SeatHold hold = inventoryService.hold(
                    requisicao.vooId(), requisicao.assentoId(), requisicao.passageiroId(),
                    TTL_PADRAO_DO_ITEM_DE_LOTE);
            inventoryService.confirm(hold.holdId());
            return new BatchItemResult(requisicao, true, null);
        } catch (RuntimeException e) {
            return new BatchItemResult(requisicao, false, e.getMessage());
        }
    }
}
