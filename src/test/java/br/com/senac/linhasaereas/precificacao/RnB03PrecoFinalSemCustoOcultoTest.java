package br.com.senac.linhasaereas.precificacao;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-B03 — Preço final sem custo oculto e congelamento de sessão.
 * Origem: RF-03; RNF-01; RNF-14; RF-14; ADR-07 [proposta, não decisão].
 */
class RnB03PrecoFinalSemCustoOcultoTest {

    private static final String VOO = "VOO-200";
    private static final String SESSAO = "sessao-1";

    @Test
    void rnB03_cf_precoNaBuscaIgualAoPrecoNoCheckoutMesmaSessao() {
        // Arrange
        FarePriceService service = new FarePriceService();

        // Act
        BigDecimal precoBusca = service.precoNaBusca(VOO, SESSAO);
        BigDecimal precoCheckout = service.precoNoCheckout(VOO, SESSAO);

        // Assert: preço no checkout idêntico ao exibido na busca (0% de divergência)
        assertEquals(precoBusca, precoCheckout);
    }

    @Test
    void rnB03_conf_precoCongeladoDentroDaSessaoMesmoAposMudancaDeTarifaDinamica() {
        // Arrange
        FarePriceService service = new FarePriceService();
        BigDecimal precoOriginal = service.precoNaBusca(VOO, SESSAO);

        // Act: tarifa dinâmica muda enquanto a mesma sessão continua ativa
        service.atualizarTarifaDinamica(VOO, precoOriginal.add(BigDecimal.TEN), Instant.now());
        BigDecimal precoCheckoutMesmaSessao = service.precoNoCheckout(VOO, SESSAO);

        // Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 2,
        // ADR-07 adotado) — o preço é congelado por sessão (0% de divergência); a mudança de tarifa
        // dinâmica não pode afetar a mesma sessão. Conflito C-02 fechado.
        assertEquals(precoOriginal, precoCheckoutMesmaSessao);
    }

    @Test
    void rnB03_cf_tarifaDinamicaPodeMudarEntreSessoesDistintasComSinalizacaoAntesDaConfirmacao() {
        // Arrange
        FarePriceService service = new FarePriceService();
        BigDecimal precoSessaoAnterior = service.precoNaBusca(VOO, SESSAO);

        // Act: tarifa dinâmica é atualizada e uma nova sessão consulta o preço no checkout
        service.atualizarTarifaDinamica(VOO, precoSessaoAnterior.add(BigDecimal.TEN),
                Instant.now().plus(Duration.ofMinutes(5)));
        PrecoComSinalizacao resultado = service.precoNoCheckoutComSinalizacaoDeAlteracao(VOO, "sessao-2");

        // Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 2,
        // ADR-07 adotado) — tarifa dinâmica pode variar entre sessões distintas (a cada >=5min), desde
        // que sinalizada antes da confirmação.
        assertTrue(resultado.tarifaAlteradaDesdeAUltimaSessao(),
                "alteração de tarifa entre sessões distintas deve ser sinalizada antes da confirmação");
    }

    @Test
    void rnB03_lim_atualizacaoDeTarifaDinamicaExatamenteNosCincoMinutosEPermitidaComSinalizacao() {
        // Arrange
        FarePriceService service = new FarePriceService();
        Instant momentoDaBusca = Instant.now();
        BigDecimal precoInicial = service.precoNaBusca(VOO, SESSAO);

        // Act: atualização de tarifa ocorre exatamente no limiar de 5 minutos após a busca anterior
        service.atualizarTarifaDinamica(VOO, precoInicial.add(BigDecimal.ONE),
                momentoDaBusca.plus(Duration.ofMinutes(5)));
        PrecoComSinalizacao resultado = service.precoNoCheckoutComSinalizacaoDeAlteracao(VOO, "sessao-3");

        // Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 2) —
        // 5 minutos é o próprio limiar permitido para a atualização de tarifa dinâmica entre sessões,
        // portanto já considerado válido e sinalizável.
        assertTrue(resultado.tarifaAlteradaDesdeAUltimaSessao(),
                "atualização exatamente no limiar de 5 minutos já deve ser considerada válida e sinalizada");
    }

    @Test
    void rnB03_proib_precoFinalMaiorNoCheckoutSemSinalizacaoMesmaSessao() {
        // Arrange
        FarePriceService service = new FarePriceService();

        // Act & Assert: custo adicional não sinalizado previamente é proibido, mesma sessão
        assertThrows(PriceMismatchException.class, () -> service.precoNoCheckout(VOO, SESSAO));
    }

    @Test
    void rnB03_cf_composicaoDoPrecoFinalNaBuscaIncluiTaxasBagagemEEncargos() {
        // Arrange
        FarePriceService service = new FarePriceService();

        // Act
        PrecoComposto composicao = service.composicaoDoPreco(VOO);

        // Assert: preço exibido nos resultados já inclui todos os componentes, sem revelação
        // adicional apenas no checkout
        BigDecimal somaComponentes = composicao.tarifaBase()
                .add(composicao.taxas())
                .add(composicao.bagagem())
                .add(composicao.encargos());
        assertEquals(somaComponentes, composicao.total());
    }
}
