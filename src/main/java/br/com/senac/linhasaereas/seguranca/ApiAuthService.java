package br.com.senac.linhasaereas.seguranca;

/**
 * Autenticação OAuth2 e expiração de tokens de API (RNF-28, RN-H04).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class ApiAuthService {

    private static final java.time.Duration VALIDADE_MAXIMA = java.time.Duration.ofHours(1);

    public AccessToken issueToken(String clientId) {
        java.time.Instant emitidoEm = java.time.Instant.now();
        return new AccessToken("token-" + java.util.UUID.randomUUID(), emitidoEm, emitidoEm.plus(VALIDADE_MAXIMA));
    }
}
