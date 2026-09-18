package br.com.senac.linhasaereas.notificacao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-C04 — Confirmação e localizador enviados automaticamente (e-mail + WhatsApp).
 * Origem: RF-10; RNF-11.
 */
class RnC04ConfirmacaoELocalizadorTest {

    @Test
    void rnC04_cf_pagamentoAprovadoEnviaConfirmacaoPorEmailEWhatsappEmAte30s() {
        // Arrange
        NotificacaoService service = new NotificacaoService();
        Reserva reserva = new Reserva("reserva-1", "ABC123");

        // Act
        EnvioResultado resultado = service.enviarConfirmacao(reserva);

        // Assert
        assertTrue(resultado.emailEnviado(), "confirmação deve ser enviada por e-mail");
        assertTrue(resultado.whatsappEnviado(), "confirmação deve ser enviada por WhatsApp");
        assertTrue(resultado.duracao().compareTo(Duration.ofSeconds(30)) <= 0, "envio deve ocorrer em até 30s");
    }

    @Test
    void rnC04_lim_envioExatamenteAos30SegundosAindaDentroDoSla() {
        // Arrange
        NotificacaoService service = new NotificacaoService();
        Reserva reserva = new Reserva("reserva-2", "DEF456");

        // Act
        EnvioResultado resultado = service.enviarConfirmacao(reserva);

        // Assert
        assertEquals(Duration.ofSeconds(30), resultado.duracao());
    }

    @Test
    @Disabled("Lacuna: comportamento de fallback quando um dos canais (ex.: WhatsApp) falha não é definido "
            + "pela fonte; a meta de ≥99%/mês é agregada, não por evento (plano-tdd.md, Seção 2.3 RN-C04). "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnC04_conf_falhaDeEntregaEmUmDosCanais_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    void rnC04_proib_reservaConfirmadaSemQualquerNotificacaoEnviadaEProibida() {
        // Arrange: falha total de notificação, sem novo envio/reprocessamento
        NotificacaoService service = new NotificacaoService();
        Reserva reserva = new Reserva("reserva-3", "GHI789");

        // Act
        EnvioResultado resultado = service.enviarConfirmacao(reserva);

        // Assert: uma reserva confirmada nunca pode ficar sem qualquer notificação enviada (RNF-19)
        assertTrue(resultado.emailEnviado() || resultado.whatsappEnviado(),
                "deve haver reprocessamento até que ao menos um canal confirme a entrega");
    }
}
