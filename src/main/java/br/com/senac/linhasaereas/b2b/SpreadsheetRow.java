package br.com.senac.linhasaereas.b2b;

/** Linha de uma planilha de emissão em lote B2B (RN-E03). */
public record SpreadsheetRow(
        int numeroLinha,
        String cpf,
        String nomePassageiro,
        String vooId,
        String assentoDesejado
) {
}
