package br.com.senac.linhasaereas.lote;

import br.com.senac.linhasaereas.estoque.SeatInventoryService;

import java.util.List;

/**
 * Emissão em lote a partir de planilha — arquitetura.md §9, §11.3, §12.6 (RN-A06/RN-E03).
 * Cada item deve reusar o mesmo mecanismo de exclusividade de assento do Estoque (RN-A01/RN-A03).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class BatchIssuanceService {

    private final SeatInventoryService inventoryService;

    public BatchIssuanceService(SeatInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public BatchResult processBatch(List<PassengerSeatRequest> requisicoes) {
        throw new UnsupportedOperationException("processamento de lote ainda não implementado (RN-A06)");
    }
}
