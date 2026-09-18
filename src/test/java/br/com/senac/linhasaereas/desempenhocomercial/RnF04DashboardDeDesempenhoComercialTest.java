package br.com.senac.linhasaereas.desempenhocomercial;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-F04 — Dashboard de desempenho comercial.
 * Origem: RF-27; RNF-07.
 */
class RnF04DashboardDeDesempenhoComercialTest {

    @Test
    void rnF04_cf_consultaDeAte90DiasSemTimeoutECarregamentoRapido() {
        // Arrange
        DashboardComercialService service = new DashboardComercialService();
        ConsultaDesempenhoRequest request = new ConsultaDesempenhoRequest(
                "companhia-1", LocalDate.now().minusDays(90), LocalDate.now());

        // Act
        ResultadoDashboard resultado = service.consultar(request);

        // Assert: sem timeout, carregamento <= 4s [baseline sugerida]
        assertFalse(resultado.timeout());
        assertTrue(resultado.tempoCarregamento().compareTo(Duration.ofSeconds(4)) <= 0,
                "carregamento deve ocorrer em até 4s");
    }

    @Test
    void rnF04_lim_consultaDeExatamente12MesesDeHistoricoCarregaDentroDoLimite() {
        // Arrange: período no limite máximo suportado
        DashboardComercialService service = new DashboardComercialService();
        ConsultaDesempenhoRequest request = new ConsultaDesempenhoRequest(
                "companhia-1", LocalDate.now().minusMonths(12), LocalDate.now());

        // Act
        ResultadoDashboard resultado = service.consultar(request);

        // Assert
        assertFalse(resultado.timeout());
        assertTrue(resultado.tempoCarregamento().compareTo(Duration.ofSeconds(4)) <= 0,
                "carregamento deve ocorrer em até 4s, mesmo no limite de 12 meses");
    }

    @Test
    @Disabled("Comportamento para consulta de período superior a 12 meses de histórico não é definido "
            + "pela fonte — lacuna registrada no plano-tdd.md, Seção 2.6, RN-F04, caso INV. "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnF04_inv_consultaDePeriodoSuperiorA12Meses_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
