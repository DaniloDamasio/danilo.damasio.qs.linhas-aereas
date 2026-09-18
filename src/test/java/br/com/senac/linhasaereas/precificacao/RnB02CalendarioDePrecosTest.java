package br.com.senac.linhasaereas.precificacao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-B02 — Calendário de preços (±15 dias mínimo).
 * Origem: RF-02.
 */
class RnB02CalendarioDePrecosTest {

    @Test
    void rnB02_cf_calendarioCobreNoMinimoQuinzeDiasAntesEDepois() {
        // Arrange
        PriceCalendarService service = new PriceCalendarService();
        LocalDate dataCentral = LocalDate.now();

        // Act
        Map<LocalDate, PrecoDoDia> calendario = service.calendario(dataCentral);

        // Assert: calendário exibe preços de, no mínimo, D-15 a D+15
        assertTrue(calendario.containsKey(dataCentral.minusDays(15)));
        assertTrue(calendario.containsKey(dataCentral.plusDays(15)));
    }

    @Test
    void rnB02_lim_extremosExatosDMenos15EDMais15EstaoCobertos() {
        // Arrange
        PriceCalendarService service = new PriceCalendarService();
        LocalDate dataCentral = LocalDate.now();
        LocalDate extremoInferior = dataCentral.minusDays(15);
        LocalDate extremoSuperior = dataCentral.plusDays(15);

        // Act
        Map<LocalDate, PrecoDoDia> calendario = service.calendario(dataCentral);

        // Assert: ambos os extremos ("no mínimo ±15 dias") devem estar cobertos
        assertTrue(calendario.containsKey(extremoInferior), "extremo D-15 deve estar coberto");
        assertTrue(calendario.containsKey(extremoSuperior), "extremo D+15 deve estar coberto");
    }

    @Test
    @Disabled("Lacuna: o comportamento de exibição de um dia sem voo cadastrado dentro da janela "
            + "(omitido vs. exibido como indisponível) não é detalhado pela fonte "
            + "(plano-tdd.md Seção 3, RN-B02).")
    void rnB02_inv_diaSemVooCadastradoExibicao_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
