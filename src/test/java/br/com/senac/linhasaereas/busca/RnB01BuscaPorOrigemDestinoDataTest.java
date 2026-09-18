package br.com.senac.linhasaereas.busca;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-B01 — Busca por origem, destino e data/período com filtros de urgência.
 * Origem: RF-01.
 */
class RnB01BuscaPorOrigemDestinoDataTest {

    private static final String ORIGEM = "BH";
    private static final String DESTINO = "SP";

    @Test
    void rnB01_cf_buscaSimples() {
        // Arrange
        FlightSearchService service = new FlightSearchService();
        BuscaCriterios criterios = new BuscaCriterios(ORIGEM, DESTINO, LocalDate.now().plusDays(1), false);

        // Act
        List<Flight> resultado = service.buscar(criterios);

        // Assert: a busca deve retornar a lista de voos correspondentes
        assertFalse(resultado.isEmpty(), "busca simples com origem/destino/data válidos deve retornar voos");
    }

    @Test
    @Disabled("Lacuna: a janela de 'próximas horas' (RF-01) não é quantificada pela fonte "
            + "(plano-tdd.md Seção 3.2). Não é possível escrever asserção definitiva sobre "
            + "quais voos entram no filtro sem essa definição.")
    void rnB01_cf_filtroProximasHoras_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    void rnB01_inv_buscaPorRotaInternacionalERejeitadaPorEstarForaDeEscopo() {
        // Arrange
        FlightSearchService service = new FlightSearchService();
        BuscaCriterios criterios = new BuscaCriterios(ORIGEM, "NYC", LocalDate.now().plusDays(1), false);

        // Act & Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 8) —
        // escopo internacional confirmado como fora de escopo; a plataforma cobre apenas rotas domésticas
        // (Brasil). Suposição S-03 convertida em fato confirmado (Seção 3.3).
        assertThrows(InternationalRouteNotSupportedException.class, () -> service.buscar(criterios));
    }

    @Test
    void rnB01_inv_origemIgualDestino() {
        // Arrange
        FlightSearchService service = new FlightSearchService();
        BuscaCriterios criterios = new BuscaCriterios(ORIGEM, ORIGEM, LocalDate.now().plusDays(1), false);

        // Act & Assert: origem igual a destino não é uma busca válida
        assertThrows(InvalidSearchException.class, () -> service.buscar(criterios));
    }

    @Test
    @Disabled("Lacuna: a fonte não define explicitamente se uma data no passado deve ser "
            + "rejeitada ou apenas retornar sem resultados (plano-tdd.md Seção 3.2, RN-B01). "
            + "Não é possível escrever uma única asserção definitiva sem essa decisão.")
    void rnB01_inv_dataNoPassado_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    @Disabled("Lacuna: comportamento de fronteira na virada do dia (23:59 -> 00:00) para o "
            + "critério 'amanhã' não é definido pela fonte (plano-tdd.md Seção 3, RN-B01).")
    void rnB01_lim_buscaNoLimiteDaViradaDoDia_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
