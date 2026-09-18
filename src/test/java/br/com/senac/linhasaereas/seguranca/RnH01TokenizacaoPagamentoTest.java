package br.com.senac.linhasaereas.seguranca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RN-H01 — Tokenização PCI-DSS de dados de pagamento.
 * Origem: RNF-24. (Casos de pagamento específicos já em RN-C03; aqui, a regra de dados.)
 */
class RnH01TokenizacaoPagamentoTest {

    private static final String PAN_EM_TEXTO_CLARO = "4111111111111111";

    @Test
    void rnH01_cf_cartaoSalvoParaComprasFuturasEArmazenadoComoTokenNuncaComoPan() {
        // Arrange
        PaymentCardVaultService vault = new PaymentCardVaultService();

        // Act
        CardToken token = vault.tokenize(PAN_EM_TEXTO_CLARO);

        // Assert
        assertNotNull(token);
        assertNotEquals(PAN_EM_TEXTO_CLARO, token.token(), "o token nunca deve ser o próprio PAN em texto claro");
    }

    @Test
    void rnH01_proib_nenhumArmazenamentoPersistentePodeConterPanEmTextoClaro() {
        // Arrange
        PaymentCardVaultService vault = new PaymentCardVaultService();
        CardToken token = vault.tokenize(PAN_EM_TEXTO_CLARO);

        // Act
        String representacaoPersistida = vault.representacaoPersistida(token.token());

        // Assert: proibido de forma absoluta — 100% tokenizado, nunca PAN em texto claro
        assertFalse(representacaoPersistida.contains(PAN_EM_TEXTO_CLARO),
                "nenhum armazenamento persistente pode conter o PAN em texto claro");
    }
}
