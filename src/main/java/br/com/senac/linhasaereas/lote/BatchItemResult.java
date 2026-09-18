package br.com.senac.linhasaereas.lote;

/** Resultado individual de um item processado em lote (RN-A06). */
public record BatchItemResult(PassengerSeatRequest requisicao, boolean sucesso, String motivoFalha) {
}
