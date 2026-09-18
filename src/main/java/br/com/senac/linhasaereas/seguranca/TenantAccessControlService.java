package br.com.senac.linhasaereas.seguranca;

/**
 * RBAC e isolamento cross-tenant (RNF-27, RN-H03).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class TenantAccessControlService {

    public RecursoDoTenant acessarRecurso(String tenantSolicitanteId, String recursoId, String tenantDonoId) {
        throw new UnsupportedOperationException("controle de acesso cross-tenant ainda não implementado (RN-H03)");
    }
}
