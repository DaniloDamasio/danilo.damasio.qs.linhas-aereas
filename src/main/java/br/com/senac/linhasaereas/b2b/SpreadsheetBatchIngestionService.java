package br.com.senac.linhasaereas.b2b;

/**
 * Emissão em lote a partir de planilha — aspectos de ingestão/planilha (RN-E03).
 * Origem: RF-21; RNF-08. (Casos de exclusividade de assento já cobertos em RN-A06.)
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class SpreadsheetBatchIngestionService {

    public SpreadsheetIngestionResult ingest(byte[] conteudoPlanilha, String nomeArquivo) {
        throw new UnsupportedOperationException(
                "ingestão de planilha de emissão em lote B2B ainda não implementada (RN-E03)");
    }
}
