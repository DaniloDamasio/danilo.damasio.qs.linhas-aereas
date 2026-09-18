package br.com.senac.linhasaereas.desempenhocomercial;

import java.time.LocalDate;

/**
 * Filtro de período para consulta ao dashboard de desempenho comercial — RN-F04 (RF-27; RNF-07).
 */
public record ConsultaDesempenhoRequest(
        String companhiaId,
        LocalDate periodoInicio,
        LocalDate periodoFim
) {
}
