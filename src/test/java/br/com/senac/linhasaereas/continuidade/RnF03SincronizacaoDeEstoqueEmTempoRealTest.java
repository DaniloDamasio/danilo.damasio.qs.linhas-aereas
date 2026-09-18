package br.com.senac.linhasaereas.continuidade;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-F03 — Sincronização de estoque em tempo real (prevenção de overbooking).
 * Origem: RF-26; RNF-06; RNF-10; RNF-22. Casos centrais já cobertos em RN-A05;
 * aqui, aspectos específicos de RTO/RPO (baseline sugerida — S-01, plano-tdd.md Seção 3.3).
 */
class RnF03SincronizacaoDeEstoqueEmTempoRealTest {

    @Test
    void rnF03_conf_falhaDeSincronizacaoPorPeriodoProlongadoRecuperaDentroDoRtoERpo() {
        // Arrange
        SincronizacaoContinuidadeService service = new SincronizacaoContinuidadeService();
        Instant inicio = Instant.now().minus(Duration.ofMinutes(10));
        Instant fim = Instant.now();
        FalhaDeSincronizacao falha = new FalhaDeSincronizacao("companhia-1", inicio, fim);

        // Act
        RecuperacaoResultado resultado = service.recuperarAposFalha(falha);

        // Assert: RTO <= 5 min, RPO <= 1 min [baseline sugerida]
        assertTrue(resultado.tempoRecuperacao().compareTo(Duration.ofMinutes(5)) <= 0,
                "RTO deve ser de até 5 minutos");
        assertTrue(resultado.dadosPerdidos().compareTo(Duration.ofMinutes(1)) <= 0,
                "RPO deve ser de até 1 minuto");
    }

    @Test
    void rnF03_proib_perdaDeDadosDeEstoqueAcimaDoRpoDefinidoEProibida() {
        // Arrange: perda observada acima do RPO de 1 minuto
        SincronizacaoContinuidadeService service = new SincronizacaoContinuidadeService();
        Duration perdaAcimaDoRpo = Duration.ofMinutes(2);

        // Act & Assert
        assertThrows(PerdaDeDadosAlemDoRpoException.class,
                () -> service.validarPerdaDentroDoRpo(perdaAcimaDoRpo));
    }
}
