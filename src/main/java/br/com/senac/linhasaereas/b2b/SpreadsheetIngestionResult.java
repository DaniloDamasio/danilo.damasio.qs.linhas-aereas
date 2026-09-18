package br.com.senac.linhasaereas.b2b;

import java.util.List;

/** Resultado agregado da ingestão de uma planilha de emissão em lote B2B (RN-E03). */
public record SpreadsheetIngestionResult(
        List<SpreadsheetRow> linhasAceitas,
        List<SpreadsheetRowError> linhasRejeitadas
) {
}
