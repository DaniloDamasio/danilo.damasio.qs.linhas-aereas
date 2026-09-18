package br.com.senac.linhasaereas.seguranca;

/**
 * RBAC e isolamento cross-tenant (RNF-27, RN-H03).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class TenantAccessControlService {

    public RecursoDoTenant acessarRecurso(String tenantSolicitanteId, String recursoId, String tenantDonoId) {
        if (!tenantSolicitanteId.equals(tenantDonoId)) {
            throw new CrossTenantAccessDeniedException(
                    "tenant " + tenantSolicitanteId + " não pode acessar recursos de " + tenantDonoId);
        }
        return new RecursoDoTenant(recursoId, tenantDonoId);
    }
}
