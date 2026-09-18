package br.com.senac.linhasaereas.estoque;

/** Varredura ativa que libera HOLDs vencidos — arquitetura.md §12.4 (RN-A04). */
public class HoldSweeper {

    private final SeatInventoryService inventoryService;

    public HoldSweeper(SeatInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** @return quantidade de HOLDs transicionados para AVAILABLE nesta varredura. */
    public int runSweep() {
        throw new UnsupportedOperationException("sweeper ainda não implementado (RN-A04)");
    }
}
