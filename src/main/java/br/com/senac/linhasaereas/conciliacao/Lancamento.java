package br.com.senac.linhasaereas.conciliacao;

/** Lançamento gerado pela conciliação a partir de um evento financeiro (RN-G04). */
public record Lancamento(
        String eventoId,
        String chaveIdempotencia,
        double valor
) {
}
