package br.com.senac.linhasaereas.b2b;

import java.time.Instant;

/**
 * API pública para integração B2B (RN-E04).
 * Origem: RF-22; RNF-09; RNF-28; RNF-33.
 */
public class B2bApiGatewayService {

    public ApiResponse handle(ApiToken token, ApiRequest requisicao) {
        if (token == null) {
            throw new UnauthorizedApiAccessException("chamada à API B2B sem autenticação OAuth2 (RNF-28)");
        }
        if (token.expiraEm() == null || !token.expiraEm().isAfter(Instant.now())) {
            throw new UnauthorizedApiAccessException("token OAuth2 expirado ou inválido");
        }
        return new ApiResponse(200, "ok");
    }
}
