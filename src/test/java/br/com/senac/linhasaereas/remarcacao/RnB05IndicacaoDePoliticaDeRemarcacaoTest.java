package br.com.senac.linhasaereas.remarcacao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-B05 — Indicação de política de remarcação/cancelamento antes da compra.
 * Origem: RF-05.
 *
 * Nota: a linha "Valores concretos de multa/regra por tarifa" da tabela do plano é um Gap
 * (lacuna L-04), não um caso de teste — não há método de teste correspondente a ela.
 */
class RnB05IndicacaoDePoliticaDeRemarcacaoTest {

    private static final String VOO = "VOO-300";

    @Test
    void rnB05_cf_tarifaComRemarcacaoSemCustoIndicacaoVisivelAntesDaCompra() {
        // Arrange
        CancellationPolicyService service = new CancellationPolicyService();

        // Act
        RemarcacaoPolicy politica = service.politicaDe(VOO);

        // Assert: indicação visível antes da compra de que a remarcação não tem custo
        assertTrue(politica.semCustoDeRemarcacao());
    }

    @Test
    void rnB05_cf_tarifaComMultaDeRemarcacaoExibidaAntesDaCompra() {
        // Arrange
        CancellationPolicyService service = new CancellationPolicyService();

        // Act
        RemarcacaoPolicy politica = service.politicaDe(VOO);

        // Assert: regra de multa deve ser exibida (valor não nulo) antes da compra
        assertNotNull(politica.valorMulta());
    }

    @Test
    void rnB05_proib_compraConcluidaSemExibirPoliticaDeRemarcacao() {
        // Arrange
        CancellationPolicyService service = new CancellationPolicyService();

        // Act & Assert: RF-05 exige exibição "antes da compra" — não exibir é proibido
        assertThrows(PolicyNotDisplayedException.class,
                () -> service.confirmarCompra(VOO, false));
    }
}
