package br.com.senac.linhasaereas.b2b;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Emissão em lote a partir de planilha — aspectos de ingestão/planilha (RN-E03).
 * Origem: RF-21; RNF-08. (Casos de exclusividade de assento já cobertos em RN-A06.)
 */
public class SpreadsheetBatchIngestionService {

    private static final String EXTENSAO_SUPORTADA = ".csv";

    public SpreadsheetIngestionResult ingest(byte[] conteudoPlanilha, String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.toLowerCase().endsWith(EXTENSAO_SUPORTADA)) {
            throw new UnsupportedSpreadsheetFormatException(
                    "formato de planilha não suportado: " + nomeArquivo);
        }

        String conteudo = new String(conteudoPlanilha, StandardCharsets.UTF_8);
        String[] linhas = conteudo.split("\r?\n");

        List<SpreadsheetRow> aceitas = new ArrayList<>();
        List<SpreadsheetRowError> rejeitadas = new ArrayList<>();

        for (int i = 1; i < linhas.length; i++) {
            String linha = linhas[i];
            if (linha.isBlank()) {
                continue;
            }
            int numeroLinha = i;
            String[] campos = linha.split(",", -1);
            String cpf = campo(campos, 0);
            String nome = campo(campos, 1);
            String vooId = campo(campos, 2);
            String assento = campo(campos, 3);

            if (cpf.isBlank() || nome.isBlank() || vooId.isBlank() || assento.isBlank()) {
                rejeitadas.add(new SpreadsheetRowError(numeroLinha, "dados obrigatórios ausentes"));
            } else {
                aceitas.add(new SpreadsheetRow(numeroLinha, cpf, nome, vooId, assento));
            }
        }

        return new SpreadsheetIngestionResult(aceitas, rejeitadas);
    }

    private String campo(String[] campos, int indice) {
        return indice < campos.length ? campos[indice].trim() : "";
    }
}
