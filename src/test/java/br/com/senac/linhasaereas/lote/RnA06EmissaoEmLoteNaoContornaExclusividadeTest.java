package br.com.senac.linhasaereas.lote;

import br.com.senac.linhasaereas.estoque.SeatInventoryService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-A06 — Emissão em lote não contorna exclusividade.
 * Origem: RF-21; RNF-08; arquitetura.md §9, §11.3, §12.6.
 */
class RnA06EmissaoEmLoteNaoContornaExclusividadeTest {

    private PassengerSeatRequest passageiro(int indice, String voo) {
        return new PassengerSeatRequest("passageiro-" + indice, voo, "assento-" + indice);
    }

    private SeatInventoryService inventarioComAssentos(String voo, int quantidade) {
        Set<String> assentos = new HashSet<>();
        for (int i = 1; i <= quantidade; i++) {
            assentos.add("assento-" + i);
        }
        Map<String, Set<String>> mapa = new HashMap<>();
        mapa.put(voo, assentos);
        return new SeatInventoryService(mapa);
    }

    @Test
    void rnA06_cf_loteDeOitoPassageirosTodosOsAssentosDisponiveisTodosConfirmados() {
        // Arrange
        String voo = "VOO-200";
        SeatInventoryService inventario = inventarioComAssentos(voo, 8);
        BatchIssuanceService service = new BatchIssuanceService(inventario);
        List<PassengerSeatRequest> lote = IntStream.rangeClosed(1, 8)
                .mapToObj(i -> passageiro(i, voo))
                .toList();

        // Act
        BatchResult resultado = service.processBatch(lote);

        // Assert
        assertEquals(8, resultado.itens().size());
        assertTrue(resultado.itens().stream().allMatch(BatchItemResult::sucesso));
    }

    @Test
    void rnA06_lim_loteNoLimiteMaximoDeCinquentaPassageirosEProcessadoCompletamente() {
        // Arrange
        String voo = "VOO-201";
        SeatInventoryService inventario = inventarioComAssentos(voo, 50);
        BatchIssuanceService service = new BatchIssuanceService(inventario);
        List<PassengerSeatRequest> lote = IntStream.rangeClosed(1, 50)
                .mapToObj(i -> passageiro(i, voo))
                .toList();

        // Act
        BatchResult resultado = service.processBatch(lote);

        // Assert
        assertEquals(50, resultado.itens().size());
    }

    @Test
    void rnA06_inv_loteAcimaDoLimiteDeCinquentaPassageirosERejeitadoInteiramenteNaIngestao() {
        // Arrange
        String voo = "VOO-204";
        SeatInventoryService inventario = inventarioComAssentos(voo, 51);
        BatchIssuanceService service = new BatchIssuanceService(inventario);
        List<PassengerSeatRequest> loteAcimaDoLimite = IntStream.rangeClosed(1, 51)
                .mapToObj(i -> passageiro(i, voo))
                .toList();

        // Act & Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 9) —
        // lote acima de 50 passageiros é rejeitado inteiramente na ingestão, sem processamento parcial.
        assertThrows(BatchSizeExceededException.class, () -> service.processBatch(loteAcimaDoLimite));
    }

    @Test
    void rnA06_conf_itemDoLoteConcorreComReservaIndividualPeloMesmoAssentoApenasUmVence() {
        // Arrange: o assento disputado já está com HOLD ativo de uma sessão do canal online
        String voo = "VOO-202";
        String assentoDisputado = "assento-1";
        SeatInventoryService inventario = inventarioComAssentos(voo, 1);
        inventario.hold(voo, assentoDisputado, "sessao-online", Duration.ofMinutes(10));
        BatchIssuanceService service = new BatchIssuanceService(inventario);
        List<PassengerSeatRequest> lote = List.of(new PassengerSeatRequest("passageiro-lote", voo, assentoDisputado));

        // Act
        BatchResult resultado = service.processBatch(lote);

        // Assert: mesma regra de exclusão mútua (RN-A01/RN-A03) se aplica; o item do lote não pode vencer
        assertEquals(1, resultado.itens().size());
        assertTrue(resultado.itens().stream().noneMatch(BatchItemResult::sucesso),
                "item do lote não pode vencer um assento já possuído por outra sessão");
    }

    @Test
    void rnA06_proib_loteNuncaForcaVendaDeAssentoIndisponivel() {
        // Arrange: assento já CONFIRMED antes de o lote ser processado
        String voo = "VOO-203";
        String assento = "assento-1";
        SeatInventoryService inventario = inventarioComAssentos(voo, 1);
        var hold = inventario.hold(voo, assento, "sessao-existente", Duration.ofMinutes(10));
        inventario.confirm(hold.holdId());
        BatchIssuanceService service = new BatchIssuanceService(inventario);
        List<PassengerSeatRequest> lote = List.of(new PassengerSeatRequest("passageiro-1", voo, assento));

        // Act
        BatchResult resultado = service.processBatch(lote);

        // Assert: item reportado como falha individual; nenhuma venda forçada (arquitetura.md §11.3)
        BatchItemResult item = resultado.itens().get(0);
        assertTrue(!item.sucesso(), "assento indisponível nunca pode ser forçado a sucesso pelo lote");
        assertTrue(item.motivoFalha() != null && !item.motivoFalha().isBlank(),
                "falha do item deve ser reportada individualmente com motivo");
    }
}
