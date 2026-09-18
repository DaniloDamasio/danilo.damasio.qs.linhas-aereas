package br.com.senac.linhasaereas.busca;

import java.time.LocalDate;

/**
 * Critérios de busca de voos — RF-01 (RN-B01).
 * Dado simples, sem regra de negócio: nenhuma validação é feita aqui.
 */
public record BuscaCriterios(
        String origem,
        String destino,
        LocalDate dataViagem,
        boolean filtroProximasHoras
) {
}
