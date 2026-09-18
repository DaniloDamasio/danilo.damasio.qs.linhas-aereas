package br.com.senac.linhasaereas.b2b;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-E04 — API pública para integração B2B.
 * Origem: RF-22; RNF-09; RNF-28; RNF-33.
 */
class RnE04ApiPublicaB2bTest {

    @Test
    void rnE04_cf_chamadaAutenticadaViaOAuth2ComTokenValidoEProcessada() {
        // Arrange
        B2bApiGatewayService gateway = new B2bApiGatewayService();
        ApiToken tokenValido = new ApiToken("token-valido", Instant.now().plus(Duration.ofMinutes(30)));
        ApiRequest requisicao = new ApiRequest("/v2/voos", "v2", 0);

        // Act
        ApiResponse resposta = gateway.handle(tokenValido, requisicao);

        // Assert
        assertEquals(200, resposta.statusCode());
    }

    @Test
    @Disabled("Lacuna menor (plano-tdd.md §2.5, RN-E04 LIM): a fonte não detalha se o token deve ser aceito ou "
            + "rejeitado no instante exato de expiração (fronteira). Não escrever asserção definitiva até essa "
            + "decisão.")
    void rnE04_lim_tokenNoLimiteExatoDeExpiracao_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    void rnE04_inv_chamadaComTokenExpiradoOuInvalidoERejeitada() {
        // Arrange
        B2bApiGatewayService gateway = new B2bApiGatewayService();
        ApiToken tokenVencido = new ApiToken("token-vencido", Instant.now().minus(Duration.ofMinutes(1)));
        ApiRequest requisicao = new ApiRequest("/v2/voos", "v2", 0);

        // Act & Assert: requisição rejeitada (401/erro de autenticação)
        assertThrows(UnauthorizedApiAccessException.class, () -> gateway.handle(tokenVencido, requisicao));
    }

    @Test
    void rnE04_proib_chamadaSemAutenticacaoEBloqueada() {
        // Arrange
        B2bApiGatewayService gateway = new B2bApiGatewayService();
        ApiRequest requisicao = new ApiRequest("/v2/voos", "v2", 0);

        // Act & Assert: RNF-28 exige OAuth2 obrigatório — chamada sem token deve ser bloqueada
        assertThrows(UnauthorizedApiAccessException.class, () -> gateway.handle(null, requisicao));
    }

    @Test
    void rnE04_conf_chamadaAVersaoDescontinuadaDentroDaJanelaDeRetrocompatibilidadeContinuaFuncionando() {
        // Arrange: cliente usando /v1/ enquanto /v2/ já existe há 11 meses
        B2bApiGatewayService gateway = new B2bApiGatewayService();
        ApiToken tokenValido = new ApiToken("token-valido", Instant.now().plus(Duration.ofMinutes(30)));
        ApiRequest requisicaoNaVersaoAntiga = new ApiRequest("/v1/voos", "v1", 11);

        // Act
        ApiResponse resposta = gateway.handle(tokenValido, requisicaoNaVersaoAntiga);

        // Assert: retrocompatibilidade exigida por >= 12 meses (RNF-33), deve continuar funcionando
        assertEquals(200, resposta.statusCode());
    }

    @Test
    @Disabled("Lacuna (plano-tdd.md §2.5, RN-E04 INV / §3.2 L-—): a fonte (RNF-33) não define o comportamento "
            + "após o fim da janela de retrocompatibilidade de 12 meses. Não escrever asserção definitiva até "
            + "essa decisão.")
    void rnE04_inv_chamadaAVersaoDeApiForaDaJanelaDeRetrocompatibilidade_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
