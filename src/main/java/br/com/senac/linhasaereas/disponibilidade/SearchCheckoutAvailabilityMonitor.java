package br.com.senac.linhasaereas.disponibilidade;

import java.time.Duration;

/**
 * Monitor de disponibilidade de busca/checkout (RNF-20, RN-I01).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class SearchCheckoutAvailabilityMonitor {

    private static final Duration LIMITE_INDISPONIBILIDADE_MENSAL = Duration.ofMinutes(43);

    public boolean atendeMetaDeDisponibilidadeMensal(Duration indisponibilidadeAcumuladaNoMes) {
        return indisponibilidadeAcumuladaNoMes.compareTo(LIMITE_INDISPONIBILIDADE_MENSAL) <= 0;
    }
}
