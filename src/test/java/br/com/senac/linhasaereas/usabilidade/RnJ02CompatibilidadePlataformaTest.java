package br.com.senac.linhasaereas.usabilidade;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RN-J02 — Compatibilidade de plataforma (SO/navegadores).
 * Origem: RNF-32; RNF-33.
 */
class RnJ02CompatibilidadePlataformaTest {

    @Test
    void rnJ02_cf_usoNasDuasVersoesMaisRecentesDeIosAndroidChromeSafariEdgeTemSuporteCompleto() {
        // Arrange
        PlatformCompatibilityService service = new PlatformCompatibilityService();

        // Act
        SuporteResultado resultado = service.verificarSuporte("iOS", "ultima-versao");

        // Assert
        assertEquals(SuporteResultado.COMPLETO, resultado,
                "as 2 versões mais recentes de iOS/Android/Chrome/Safari/Edge devem ter suporte completo");
    }

    @Test
    @Disabled("INV: uso em versão de SO/navegador anterior às 2 últimas majors tem suporte não garantido pela "
            + "fonte — declarado como fora de escopo (RNF-32/RNF-33), sem definir comportamento concreto de "
            + "degradação. Nenhuma asserção de comportamento pode ser escrita sem inventar uma regra (ver Seção "
            + "2.10 do plano-tdd.md).")
    void rnJ02_inv_usoEmVersaoAnteriorAsDuasUltimasMajors_suporteNaoGarantidoPelaFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
