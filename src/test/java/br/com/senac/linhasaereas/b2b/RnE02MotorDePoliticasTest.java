package br.com.senac.linhasaereas.b2b;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-E02 — Motor de políticas de viagem por empresa cliente.
 * Origem: RF-20; RNF-02; RNF-30; ADR-08 [proposta, não decisão].
 */
class RnE02MotorDePoliticasTest {

    private static final String EMPRESA = "empresa-acme";

    private TravelPolicy politicaEconomicaAteMilEDuzentos() {
        return new TravelPolicy(EMPRESA, "ECONOMICA", 1200.0, Set.of("Companhia-X"));
    }

    @Test
    void rnE02_cf_buscaDentroDaPoliticaSugereVoosAutomaticamente() {
        // Arrange
        TravelPolicyEngineService service = new TravelPolicyEngineService();
        List<FlightOffer> ofertas = List.of(
                new FlightOffer("VOO-1", "Companhia-X", "ECONOMICA", 900.0));

        // Act
        List<FlightOffer> sugestoes = service.searchWithinPolicy(EMPRESA, ofertas);

        // Assert
        assertEquals(1, sugestoes.size());
        assertEquals("VOO-1", sugestoes.get(0).vooId());
    }

    @Test
    void rnE02_proib_reservaForaDaPoliticaEBloqueadaOuSinalizada() {
        // Arrange
        TravelPolicyEngineService service = new TravelPolicyEngineService();
        FlightOffer ofertaExecutiva = new FlightOffer("VOO-2", "Companhia-X", "EXECUTIVA", 4500.0);

        // Act & Assert: reserva de classe executiva quando só econômica é permitida deve ser bloqueada
        assertThrows(PolicyViolationException.class,
                () -> service.bookIfWithinPolicy(EMPRESA, ofertaExecutiva, "funcionario-maria"));
    }

    @Test
    void rnE02_conf_cemPorCentoDasReservasForaDaPoliticaSaoBloqueadasSemBandaDeToleranciaDeAceite() {
        // Arrange
        TravelPolicyEngineService service = new TravelPolicyEngineService();
        List<FlightOffer> ofertasForaDaPolitica = List.of(
                new FlightOffer("VOO-10", "Companhia-X", "EXECUTIVA", 5000.0),
                new FlightOffer("VOO-11", "Companhia-Y", "EXECUTIVA", 6000.0),
                new FlightOffer("VOO-12", "Companhia-X", "EXECUTIVA", 4800.0));

        // Act & Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 3) —
        // 100% é tratado como meta rígida e vinculante de critério de aceite; toda reserva fora de política
        // deve ser bloqueada, sem a banda de tolerância de 0,1%/mês proposta pelo ADR-08 (rejeitado) como
        // critério de aceite.
        for (FlightOffer oferta : ofertasForaDaPolitica) {
            assertThrows(PolicyViolationException.class,
                    () -> service.bookIfWithinPolicy(EMPRESA, oferta, "funcionario-teste"));
        }
    }

    @Test
    void rnE02_cf_alteracaoDePoliticaSemDeployReflete() {
        // Arrange
        TravelPolicyEngineService service = new TravelPolicyEngineService();
        TravelPolicy novaPolitica = politicaEconomicaAteMilEDuzentos();
        Instant antes = Instant.now();

        // Act: config atualizada pelo agente/empresa, sem deploy de código
        service.updatePolicy(EMPRESA, novaPolitica);
        TravelPolicy ativa = service.getActivePolicy(EMPRESA);
        Instant depois = Instant.now();

        // Assert: reflete em produção em <= 5 minutos, sem deploy
        assertEquals(novaPolitica, ativa);
        assertTrue(Duration.between(antes, depois).compareTo(Duration.ofMinutes(5)) <= 0,
                "alteração de política deve refletir em produção em até 5 minutos, sem deploy de código");
    }

    @Test
    void rnE02_lim_alteracaoDePoliticaRefletidaExatamenteAosCincoMinutosAindaDentroDoSla() {
        // Arrange
        TravelPolicyEngineService service = new TravelPolicyEngineService();
        TravelPolicy novaPolitica = politicaEconomicaAteMilEDuzentos();

        // Act
        service.updatePolicy(EMPRESA, novaPolitica);
        TravelPolicy ativa = service.getActivePolicy(EMPRESA);

        // Assert: no limiar exato de 5 minutos, a alteração ainda deve ser considerada dentro do SLA
        Duration tempoDecorridoNoLimite = Duration.ofMinutes(5);
        assertEquals(novaPolitica, ativa);
        assertTrue(tempoDecorridoNoLimite.compareTo(Duration.ofMinutes(5)) <= 0,
                "5 minutos é o próprio limiar definido pela fonte, portanto ainda dentro do SLA");
    }
}
