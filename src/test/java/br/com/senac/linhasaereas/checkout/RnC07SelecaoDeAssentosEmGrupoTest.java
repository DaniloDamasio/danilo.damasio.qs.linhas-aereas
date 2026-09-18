package br.com.senac.linhasaereas.checkout;

import br.com.senac.linhasaereas.estoque.SeatHold;
import br.com.senac.linhasaereas.estoque.SeatInventoryService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-C07 — Seleção de assentos com suporte a grupos/famílias.
 * Origem: RF-13.
 */
class RnC07SelecaoDeAssentosEmGrupoTest {

    private static final String VOO = "VOO-400";

    private SeatInventoryService inventarioComAssentos(String... assentos) {
        Map<String, Set<String>> mapa = new HashMap<>();
        mapa.put(VOO, Set.of(assentos));
        return new SeatInventoryService(mapa);
    }

    @Test
    void rnC07_cf_selecaoDeAssentosLadoALadoParaQuatroPassageiros() {
        // Arrange: mapa de assentos com 4 contíguos livres
        SeatInventoryService inventario = inventarioComAssentos("10A", "10B", "10C", "10D");
        SeatGroupSelectionService service = new SeatGroupSelectionService(inventario);
        List<String> desejados = List.of("10A", "10B", "10C", "10D");

        // Act
        List<SeatHold> holds = service.selecionarGrupo(VOO, desejados, "sessao-grupo-1", Duration.ofMinutes(10));

        // Assert: sistema permite reservar os 4 juntos (sujeito a RN-A01 no momento do HOLD de cada um)
        assertEquals(4, holds.size());
    }

    @Test
    @Disabled("Lacuna: o comportamento exato de fallback quando faltam assentos contíguos suficientes para o "
            + "grupo (indicar impossibilidade vs. confirmação parcial) não é detalhado pela fonte "
            + "(plano-tdd.md, Seção 2.3 RN-C07). Não escrever asserção definitiva até essa decisão.")
    void rnC07_conf_assentosContiguosInsuficientesParaOGrupo_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    void rnC07_conf_concorrenciaOutroUsuarioReservaAssentoDoBlocoDuranteSelecaoDoGrupo() {
        // Arrange: assento do meio do bloco é reservado por terceiro antes da confirmação do grupo
        SeatInventoryService inventario = inventarioComAssentos("20A", "20B", "20C", "20D");
        inventario.hold(VOO, "20B", "sessao-terceiro", Duration.ofMinutes(10));
        SeatGroupSelectionService service = new SeatGroupSelectionService(inventario);
        List<String> desejados = List.of("20A", "20B", "20C", "20D");

        // Act & Assert: a exclusão mútua (RN-A01/RN-A03) prevalece; grupo não pode ser confirmado
        // silenciosamente com o assento já indisponível
        assertThrows(GrupoIncompletoException.class,
                () -> service.selecionarGrupo(VOO, desejados, "sessao-grupo-2", Duration.ofMinutes(10)),
                "grupo deve ser informado da mudança de disponibilidade antes de confirmar");
    }
}
