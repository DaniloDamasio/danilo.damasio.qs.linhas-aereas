package br.com.senac.linhasaereas.checkout;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-C01 — Checkout rápido com dados salvos.
 * Origem: RF-07; RNF-05; RNF-13.
 */
class RnC01CheckoutRapidoTest {

    @Test
    void rnC01_cf_checkoutComPerfilECartaoSalvos() {
        // Arrange
        CheckoutService service = new CheckoutService();
        PerfilUsuario perfil = new PerfilUsuario("usuario-1", true, true);

        // Act
        CheckoutResultado resultado = service.iniciarCheckoutRapido(perfil);

        // Assert
        assertTrue(resultado.duracaoTotal().compareTo(Duration.ofSeconds(120)) <= 0,
                "conclusão deve ocorrer em até 120s");
        assertTrue(resultado.telas() <= 4, "no máximo 4 telas/etapas");
        assertTrue(resultado.cliques() <= 3, "no máximo 3 toques/cliques");
        assertTrue(resultado.camposObrigatoriosAdicionais() <= 2, "no máximo 2 campos obrigatórios adicionais");
    }

    @Test
    void rnC01_lim_exatamente4Telas3Cliques2CamposAdicionais() {
        // Arrange
        CheckoutService service = new CheckoutService();
        PerfilUsuario perfil = new PerfilUsuario("usuario-2", true, true);

        // Act
        CheckoutResultado resultado = service.iniciarCheckoutRapido(perfil);

        // Assert: fluxo exatamente no limite máximo ainda é considerado conforme
        assertEquals(4, resultado.telas());
        assertEquals(3, resultado.cliques());
        assertEquals(2, resultado.camposObrigatoriosAdicionais());
    }

    @Test
    void rnC01_inv_checkoutExcede4TelasOuMaisDe2CamposAdicionais() {
        // Arrange
        CheckoutService service = new CheckoutService();
        PerfilUsuario perfil = new PerfilUsuario("usuario-3", true, true);

        // Act & Assert: exceder os limites deve ser tratado como falha de aceitação (RNF-13)
        assertThrows(CheckoutNaoConformeException.class, () -> service.iniciarCheckoutRapido(perfil));
    }

    @Test
    @Disabled("Lacuna: RNF-05/RNF-13 pressupõem dados salvos; a fonte não define o SLA de checkout para "
            + "usuário novo sem perfil salvo (plano-tdd.md, Seção 2.3 RN-C01, item CF final). "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnC01_cf_checkoutSemPerfilSalvo_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
