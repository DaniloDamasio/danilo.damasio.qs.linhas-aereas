package br.com.senac.linhasaereas.disponibilidade;

import java.time.Duration;

/**
 * Monitor de disponibilidade de busca/checkout (RNF-20, RN-I01).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class SearchCheckoutAvailabilityMonitor {

    public boolean atendeMetaDeDisponibilidadeMensal(Duration indisponibilidadeAcumuladaNoMes) {
        throw new UnsupportedOperationException("cálculo de meta de disponibilidade ainda não implementado (RN-I01)");
    }
}
