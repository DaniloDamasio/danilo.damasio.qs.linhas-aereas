package br.com.senac.linhasaereas.seguranca;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-H04 — Autenticação OAuth2 e expiração de tokens para APIs.
 * Origem: RNF-28. (Casos específicos de API já cobertos em RN-E04.)
 */
class RnH04OAuthExpiracaoTokenTest {

    @Test
    void rnH04_cf_logDeAuditoriaDeChamadaDeApiERegistradoERetidoPorAoMenos5Anos() {
        // Arrange
        ApiCallAuditTrail auditTrail = new ApiCallAuditTrail();

        // Act
        AuditRecord registro = auditTrail.registrarChamadaAutenticada("chamada-1");

        // Assert
        assertTrue(registro.prazoDeRetencao().compareTo(Duration.ofDays(5 * 365)) >= 0,
                "toda chamada autenticada deve ser retida por ao menos 5 anos");
    }

    @Test
    void rnH04_proib_tokenDeAcessoValidoPorMaisDeUmaHoraEProibido() {
        // Arrange
        ApiAuthService authService = new ApiAuthService();

        // Act
        AccessToken token = authService.issueToken("cliente-1");

        // Assert: expiração máxima de 1h é proibição absoluta
        Duration validade = Duration.between(token.emitidoEm(), token.expiraEm());
        assertTrue(validade.compareTo(Duration.ofHours(1)) <= 0,
                "nenhum token de acesso pode ser válido por mais de 1 hora");
    }
}
