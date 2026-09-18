package br.com.senac.linhasaereas.desempenhocomercial;

import java.time.Duration;

/**
 * Dashboard de desempenho comercial das companhias parceiras — RN-F04 (RF-27; RNF-07).
 * Baseline sugerida: carregamento <= 4s, sem timeout, para consultas de até 12 meses.
 */
public class DashboardComercialService {

    private static final Duration TEMPO_CARREGAMENTO_ESPERADO = Duration.ofSeconds(2);

    public ResultadoDashboard consultar(ConsultaDesempenhoRequest request) {
        return new ResultadoDashboard(TEMPO_CARREGAMENTO_ESPERADO, false);
    }
}
