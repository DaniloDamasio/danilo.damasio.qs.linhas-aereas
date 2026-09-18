package br.com.senac.linhasaereas.continuidade;

import java.time.Duration;

/**
 * Resultado da recuperação após falha de sincronização — RN-F03 (RNF-06; RNF-10; RNF-22).
 */
public record RecuperacaoResultado(
        Duration tempoRecuperacao,
        Duration dadosPerdidos
) {
}
