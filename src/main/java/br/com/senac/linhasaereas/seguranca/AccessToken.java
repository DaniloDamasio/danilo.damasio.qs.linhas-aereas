package br.com.senac.linhasaereas.seguranca;

import java.time.Instant;

/** Token de acesso OAuth2 emitido para chamadas de API (RNF-28, RN-H04). */
public record AccessToken(String token, Instant emitidoEm, Instant expiraEm) {
}
