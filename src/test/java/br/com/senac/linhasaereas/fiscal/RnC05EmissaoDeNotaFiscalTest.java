package br.com.senac.linhasaereas.fiscal;

import br.com.senac.linhasaereas.notificacao.Reserva;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-C05 — Emissão automática de nota fiscal/recibo.
 * Origem: RF-11; RNF-12.
 */
class RnC05EmissaoDeNotaFiscalTest {

    @Test
    void rnC05_cf_pagamentoAprovadoEmiteNotaFiscalDisponivelPorEmailEDownload() {
        // Arrange
        NotaFiscalService service = new NotaFiscalService();
        Reserva reserva = new Reserva("reserva-1", "ABC123");

        // Act
        NotaFiscal notaFiscal = service.emitir(reserva);

        // Assert
        assertTrue(notaFiscal.disponivelPorEmail(), "NF deve estar disponível por e-mail");
        assertTrue(notaFiscal.disponivelParaDownload(), "NF deve estar disponível para download");
        assertTrue(notaFiscal.duracaoEmissao().compareTo(Duration.ofSeconds(60)) <= 0, "emissão em até 60s");
    }

    @Test
    void rnC05_lim_emissaoExatamenteAos60SegundosAindaDentroDoSla() {
        // Arrange
        NotaFiscalService service = new NotaFiscalService();
        Reserva reserva = new Reserva("reserva-2", "DEF456");

        // Act
        NotaFiscal notaFiscal = service.emitir(reserva);

        // Assert
        assertEquals(Duration.ofSeconds(60), notaFiscal.duracaoEmissao());
    }

    @Test
    @Disabled("Lacuna: comportamento de retry/fallback para indisponibilidade do integrador fiscal externo "
            + "(NF-e/SEFAZ) não é definido pela fonte (plano-tdd.md, Seção 2.3 RN-C05; suposição S-02). "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnC05_conf_falhaDoEmissorFiscalExterno_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    void rnC05_proib_emissaoNaoPodeDependerDeSolicitacaoManualDoPassageiro() {
        // Arrange: emissão deve ocorrer automaticamente, sem qualquer ação do usuário
        NotaFiscalService service = new NotaFiscalService();
        Reserva reserva = new Reserva("reserva-3", "GHI789");

        // Act
        NotaFiscal notaFiscal = service.emitir(reserva);

        // Assert: RF-11 exige emissão automática "sem necessidade de solicitação manual"
        assertTrue(notaFiscal.numero() != null && !notaFiscal.numero().isBlank(),
                "NF deve ser emitida automaticamente ao aprovar o pagamento, sem ação manual do passageiro");
    }
}
