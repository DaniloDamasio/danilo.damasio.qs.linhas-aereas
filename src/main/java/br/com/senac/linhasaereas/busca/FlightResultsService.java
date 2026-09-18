package br.com.senac.linhasaereas.busca;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Ordenação e filtros de resultados — RF-04 (RN-B04).
 */
public class FlightResultsService {

    private static final Set<String> CRITERIOS_VALIDOS = Set.of("preco");

    public List<Flight> ordenarPorPreco(List<Flight> voos) {
        return voos.stream()
                .sorted(Comparator.comparing(Flight::preco))
                .toList();
    }

    public List<Flight> ordenarPor(List<Flight> voos, String criterio) {
        if (criterio == null || !CRITERIOS_VALIDOS.contains(criterio)) {
            throw new InvalidSortCriterionException("critério de ordenação inexistente: " + criterio);
        }
        return ordenarPorPreco(voos);
    }

    public List<Flight> filtrarPorRemarcacaoFlexivel(List<Flight> voos, boolean somenteFlexiveis) {
        return voos.stream()
                .filter(voo -> voo.remarcacaoFlexivel() == somenteFlexiveis)
                .toList();
    }
}
