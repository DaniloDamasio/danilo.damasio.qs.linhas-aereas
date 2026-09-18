package br.com.senac.linhasaereas.seguranca;

/** Recurso pertencente a um tenant (companhia aérea ou empresa cliente B2B) — RN-H03. */
public record RecursoDoTenant(String recursoId, String tenantDonoId) {
}
