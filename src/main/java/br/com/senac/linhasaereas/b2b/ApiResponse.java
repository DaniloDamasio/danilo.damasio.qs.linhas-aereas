package br.com.senac.linhasaereas.b2b;

/** Resposta da API pública B2B (RN-E04). */
public record ApiResponse(int statusCode, String corpo) {
}
