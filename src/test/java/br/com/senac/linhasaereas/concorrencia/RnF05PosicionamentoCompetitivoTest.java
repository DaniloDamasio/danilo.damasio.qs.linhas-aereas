package br.com.senac.linhasaereas.concorrencia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RN-F05 — Posicionamento competitivo (ranking).
 * Origem: RF-28.
 * Nota: a metodologia de cálculo do ranking não é definida pela fonte — gap registrado no
 * plano-tdd.md, Seção 2.6/3.2 (não é caso de teste; apenas o caminho feliz de exibição é testável).
 */
class RnF05PosicionamentoCompetitivoTest {

    @Test
    void rnF05_cf_gestorConsultaRankingDeExibicaoEPrecoParaSuaRotaComConcorrenciaCadastrada() {
        // Arrange
        RankingCompetitivoService service = new RankingCompetitivoService();
        RankingConsultaRequest request = new RankingConsultaRequest("rota-BH-SP", "companhia-1");

        // Act
        RankingResultado resultado = service.consultarRanking(request);

        // Assert: ranking exibido para a rota consultada
        assertNotNull(resultado);
        assertFalse(resultado.posicoesPorCompanhia().isEmpty(), "ranking deve ser exibido com ao menos uma posição");
    }
}
