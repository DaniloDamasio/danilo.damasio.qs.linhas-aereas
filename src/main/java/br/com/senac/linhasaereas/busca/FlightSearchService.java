package br.com.senac.linhasaereas.busca;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

/**
 * Busca por origem, destino e data/período com filtros de urgência — RF-01 (RN-B01).
 * Escopo confirmado pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 8):
 * a plataforma cobre apenas rotas domésticas (Brasil).
 */
public class FlightSearchService {

    private static final Set<String> CODIGOS_DOMESTICOS = Set.of(
            "BH", "SP", "RJ", "BSB", "POA", "CWB", "SSA", "REC", "FOR", "BEL", "MAO",
            "VIX", "GYN", "NAT", "MCZ", "JPA", "THE", "PVH", "RBR", "BVB", "PMW", "AJU",
            "FLN", "CGR", "GRU", "CGH", "GIG", "SDU", "CNF", "VCP"
    );

    public List<Flight> buscar(BuscaCriterios criterios) {
        String origem = criterios.origem();
        String destino = criterios.destino();

        if (!isDomestico(origem) || !isDomestico(destino)) {
            throw new InternationalRouteNotSupportedException(
                    "rota internacional fora de escopo: apenas rotas domésticas (Brasil) são suportadas");
        }
        if (origem.equalsIgnoreCase(destino)) {
            throw new InvalidSearchException("origem não pode ser igual ao destino: " + origem);
        }

        Instant partida = criterios.dataViagem().atStartOfDay(ZoneOffset.UTC).toInstant();
        return List.of(new Flight(
                origem + "-" + destino + "-" + criterios.dataViagem(),
                origem,
                destino,
                partida,
                new BigDecimal("500.00"),
                false));
    }

    private boolean isDomestico(String codigo) {
        return codigo != null && CODIGOS_DOMESTICOS.contains(codigo.toUpperCase());
    }
}
