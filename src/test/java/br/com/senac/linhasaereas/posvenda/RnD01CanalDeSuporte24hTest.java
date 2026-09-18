package br.com.senac.linhasaereas.posvenda;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-D01 — Canal de suporte 24h.
 * Origem: RF-16; RNF-21.
 */
class RnD01CanalDeSuporte24hTest {

    @Test
    void rnD01_cf_usuarioAcionaChatAsTresDaManhaCanalDisponivel() {
        // Arrange
        SupportChannel canal = new SupportChannel();
        Instant tresDaManha = ZonedDateTime.of(2026, 9, 17, 3, 0, 0, 0, ZoneOffset.UTC).toInstant();

        // Act
        boolean disponivel = canal.isAvailable(tresDaManha);

        // Assert: canal disponível 24h/7 dias/365 dias, inclusive às 3h da manhã
        assertTrue(disponivel);
    }

    @Test
    void rnD01_lim_tempoDePrimeiraRespostaNoLimiteDeDoisMinutos() {
        // Arrange
        SupportChannel canal = new SupportChannel();
        Instant agora = Instant.now();

        // Act
        ChatSession sessao = canal.openChat(agora);

        // Assert: resposta em até 2 min [baseline sugerida]
        assertTrue(sessao.tempoAtePrimeiraResposta().compareTo(Duration.ofMinutes(2)) <= 0,
                "tempo de primeira resposta deve respeitar o limite de 2 minutos");
    }

    @Test
    void rnD01_proib_canalIndisponivelEmQualquerJanelaDoDiaNuncaOcorre() {
        // Arrange
        SupportChannel canal = new SupportChannel();
        Instant meiaNoite = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant finalDoAno = ZonedDateTime.of(2026, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC).toInstant();

        // Act
        boolean disponivelMeiaNoite = canal.isAvailable(meiaNoite);
        boolean disponivelFinalDoAno = canal.isAvailable(finalDoAno);

        // Assert: indisponibilidade em qualquer janela é proibida pela meta 24/7/365
        assertTrue(disponivelMeiaNoite);
        assertTrue(disponivelFinalDoAno);
        assertFalse(!disponivelMeiaNoite && !disponivelFinalDoAno);
    }
}
