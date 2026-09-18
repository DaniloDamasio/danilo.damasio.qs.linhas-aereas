package br.com.senac.linhasaereas.companhia;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Dados de entrada para cadastro de uma nova tarifa por um gestor comercial — RN-F01 (RF-24).
 */
public record NovaTarifaRequest(
        String gestorId,
        String rotaOrigem,
        String rotaDestino,
        LocalTime horario,
        String classe,
        BigDecimal valor,
        Duration tempoGastoNoCadastro
) {
}
