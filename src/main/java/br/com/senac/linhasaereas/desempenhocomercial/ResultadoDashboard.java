package br.com.senac.linhasaereas.desempenhocomercial;

import java.time.Duration;

/**
 * Resultado de uma consulta ao dashboard de desempenho comercial — RN-F04 (RF-27; RNF-07).
 */
public record ResultadoDashboard(
        Duration tempoCarregamento,
        boolean timeout
) {
}
