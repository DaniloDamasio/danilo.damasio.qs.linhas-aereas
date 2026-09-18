package br.com.senac.linhasaereas.b2b;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-E03 — Emissão em lote a partir de planilha (aspectos de ingestão/planilha).
 * Origem: RF-21; RNF-08. (Casos de exclusividade de assento já cobertos em RN-A06.)
 */
class RnE03EmissaoEmLotePlanilhaTest {

    @Test
    void rnE03_cf_planilhaComOitoPassageirosValidosProcessaTodosComSugestaoDentroDaPolitica() {
        // Arrange: cenário da persona — convenção em Salvador, 8 passageiros válidos
        SpreadsheetBatchIngestionService service = new SpreadsheetBatchIngestionService();
        byte[] planilhaValida = "cpf,nome,voo,assento\n...8 linhas válidas...".getBytes(StandardCharsets.UTF_8);

        // Act
        SpreadsheetIngestionResult resultado = service.ingest(planilhaValida, "convencao-salvador.csv");

        // Assert
        assertEquals(8, resultado.linhasAceitas().size());
        assertTrue(resultado.linhasRejeitadas().isEmpty());
    }

    @Test
    void rnE03_inv_linhaComDadosIncompletosERejeitadaIndividualmenteSemInterromperOLote() {
        // Arrange: uma linha com CPF ausente, entre outras válidas
        SpreadsheetBatchIngestionService service = new SpreadsheetBatchIngestionService();
        byte[] planilhaComLinhaMalformada = (
                "cpf,nome,voo,assento\n"
                        + "111.111.111-11,Fulano,VOO-1,10A\n"
                        + ",Ciclano,VOO-1,10B\n"
        ).getBytes(StandardCharsets.UTF_8);

        // Act
        SpreadsheetIngestionResult resultado = service.ingest(planilhaComLinhaMalformada, "lote.csv");

        // Assert: a linha malformada é rejeitada/reportada individualmente; o lote inteiro não é interrompido
        assertEquals(1, resultado.linhasAceitas().size());
        assertEquals(1, resultado.linhasRejeitadas().size());
        assertEquals(2, resultado.linhasRejeitadas().get(0).numeroLinha());
    }

    @Test
    void rnE03_inv_planilhaEmFormatoNaoSuportadoERejeitadaSemProcessamentoParcial() {
        // Arrange: arquivo corrompido/tipo incorreto
        SpreadsheetBatchIngestionService service = new SpreadsheetBatchIngestionService();
        byte[] arquivoCorrompido = new byte[]{0x00, 0x01, 0x02};

        // Act & Assert: upload inteiro rejeitado, nenhum processamento parcial
        assertThrows(UnsupportedSpreadsheetFormatException.class,
                () -> service.ingest(arquivoCorrompido, "arquivo.exe"));
    }

    @Test
    @Disabled("Lacuna (plano-tdd.md §2.5, RN-E03 CONF): a fonte não define comportamento de deduplicação para "
            + "passageiro da planilha com reserva conflitante ativa/duplicidade na própria planilha. "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnE03_conf_passageiroDaPlanilhaComReservaConflitanteAtivaDeduplicacao_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
