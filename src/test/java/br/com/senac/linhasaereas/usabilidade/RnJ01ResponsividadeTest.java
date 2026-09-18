package br.com.senac.linhasaereas.usabilidade;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-J01 — Responsividade e desempenho de carregamento.
 * Origem: RNF-18.
 */
class RnJ01ResponsividadeTest {

    @Test
    void rnJ01_cf_renderizacaoEmViewportMinimoDe360PxEFuncionalComLcpAte2_5s() {
        // Arrange
        ResponsiveRenderingService service = new ResponsiveRenderingService();

        // Act
        RenderResult resultado = service.renderizar(360);

        // Assert
        assertTrue(resultado.funcional(), "interface deve ser funcional no viewport mínimo suportado (360px)");
        assertTrue(resultado.largestContentfulPaint().compareTo(Duration.ofMillis(2500)) <= 0,
                "LCP deve ser <= 2,5s em 4G simulado");
    }

    @Test
    void rnJ01_lim_renderizacaoEmViewportMaximoDe1920PxEFuncional() {
        // Arrange
        ResponsiveRenderingService service = new ResponsiveRenderingService();

        // Act
        RenderResult resultado = service.renderizar(1920);

        // Assert
        assertTrue(resultado.funcional(), "interface deve ser funcional no viewport máximo suportado (1920px)");
    }

    @Test
    @Disabled("INV: viewport fora da faixa suportada (ex.: 320px, abaixo do mínimo de 360px) tem comportamento "
            + "não garantido pela fonte — lacuna registrada na Seção 2.10/RN-J01 do plano-tdd.md. Nenhuma "
            + "asserção de comportamento pode ser escrita sem inventar uma regra.")
    void rnJ01_inv_viewportForaDaFaixaSuportadaAbaixoDoMinimo_comportamentoNaoGarantidoPelaFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
