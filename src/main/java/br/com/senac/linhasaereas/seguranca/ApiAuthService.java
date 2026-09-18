package br.com.senac.linhasaereas.seguranca;

/**
 * Autenticação OAuth2 e expiração de tokens de API (RNF-28, RN-H04).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class ApiAuthService {

    public AccessToken issueToken(String clientId) {
        throw new UnsupportedOperationException("emissão de token OAuth2 ainda não implementada (RN-H04)");
    }
}
