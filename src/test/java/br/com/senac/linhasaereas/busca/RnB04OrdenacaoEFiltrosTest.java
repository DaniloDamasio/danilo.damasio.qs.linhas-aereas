package br.com.senac.linhasaereas.busca;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-B04 — Ordenação e filtros de resultados.
 * Origem: RF-04.
 */
class RnB04OrdenacaoEFiltrosTest {

    private Flight voo(String id, BigDecimal preco, boolean remarcacaoFlexivel) {
        return new Flight(id, "BH", "SP", Instant.now(), preco, remarcacaoFlexivel);
    }

    @Test
    void rnB04_cf_ordenarPorMenorPreco() {
        // Arrange
        FlightResultsService service = new FlightResultsService();
        List<Flight> voos = List.of(
                voo("V1", new BigDecimal("500"), false),
                voo("V2", new BigDecimal("300"), false),
                voo("V3", new BigDecimal("400"), false)
        );

        // Act
        List<Flight> ordenados = service.ordenarPorPreco(voos);

        // Assert: resultados em ordem crescente de preço
        assertEquals(List.of("V2", "V3", "V1"), ordenados.stream().map(Flight::id).toList());
    }

    @Test
    void rnB04_cf_filtrarPorPoliticaDeRemarcacaoFlexivel() {
        // Arrange
        FlightResultsService service = new FlightResultsService();
        List<Flight> voos = List.of(
                voo("V1", new BigDecimal("500"), true),
                voo("V2", new BigDecimal("300"), false)
        );

        // Act
        List<Flight> filtrados = service.filtrarPorRemarcacaoFlexivel(voos, true);

        // Assert: apenas voos com remarcação flexível exibidos
        assertEquals(List.of("V1"), filtrados.stream().map(Flight::id).toList());
    }

    @Test
    void rnB04_lim_listaVaziaAposFiltro() {
        // Arrange
        FlightResultsService service = new FlightResultsService();
        List<Flight> voos = List.of(voo("V1", new BigDecimal("500"), false));

        // Act
        List<Flight> filtrados = service.filtrarPorRemarcacaoFlexivel(voos, true);

        // Assert: estado vazio tratado explicitamente, sem lançar erro
        assertTrue(filtrados.isEmpty(), "filtro que elimina todos os resultados deve retornar lista vazia, não erro");
    }

    @Test
    void rnB04_inv_criterioDeOrdenacaoInexistente() {
        // Arrange
        FlightResultsService service = new FlightResultsService();
        List<Flight> voos = List.of(voo("V1", new BigDecimal("500"), false));

        // Act & Assert: parâmetro de ordenação inválido deve ser rejeitado, nunca aplicado como default silencioso
        assertThrows(InvalidSortCriterionException.class, () -> service.ordenarPor(voos, "campo_inexistente"));
    }
}
