package br.com.senac.linhasaereas.b2b;

/** Erro de uma linha individual de planilha rejeitada durante a ingestão (RN-E03). */
public record SpreadsheetRowError(int numeroLinha, String motivo) {
}
