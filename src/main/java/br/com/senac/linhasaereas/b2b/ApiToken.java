package br.com.senac.linhasaereas.b2b;

import java.time.Instant;

/** Token OAuth2 de acesso à API pública B2B (RN-E04). Origem: RNF-28 (expiração máxima de 1h). */
public record ApiToken(String valor, Instant expiraEm) {
}
