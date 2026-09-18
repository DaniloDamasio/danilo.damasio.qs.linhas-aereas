package br.com.senac.linhasaereas.conciliacao;

import java.time.Instant;

/** Evento financeiro de venda/repasse a ser conciliado (RN-G04). */
public record FinancialEvent(
        String eventoId,
        String tipo,
        double valor,
        Instant ocorridoEm
) {
}
