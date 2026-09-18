package br.com.senac.linhasaereas.busca;

import java.util.List;

/**
 * Ordenação e filtros de resultados — RF-04 (RN-B04).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class FlightResultsService {

    public List<Flight> ordenarPorPreco(List<Flight> voos) {
        throw new UnsupportedOperationException("ordenação por menor preço ainda não implementada (RN-B04)");
    }

    public List<Flight> ordenarPor(List<Flight> voos, String criterio) {
        throw new UnsupportedOperationException("ordenação por critério arbitrário ainda não implementada (RN-B04)");
    }

    public List<Flight> filtrarPorRemarcacaoFlexivel(List<Flight> voos, boolean somenteFlexiveis) {
        throw new UnsupportedOperationException("filtro por política de remarcação flexível ainda não implementado (RN-B04)");
    }
}
