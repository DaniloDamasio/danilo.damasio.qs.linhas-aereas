package br.com.senac.linhasaereas.conciliacao;

import java.util.List;

/** Relatório consolidado de um período de conciliação financeira (RN-G04). */
public record ReconciliationReport(
        String periodo,
        List<Lancamento> lancamentos
) {
}
