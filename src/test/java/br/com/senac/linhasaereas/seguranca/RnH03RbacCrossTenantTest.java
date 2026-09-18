package br.com.senac.linhasaereas.seguranca;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-H03 — RBAC e isolamento cross-tenant.
 * Origem: RNF-27; ADR-04 [proposta, não decisão].
 */
class RnH03RbacCrossTenantTest {

    @Test
    void rnH03_cf_usuarioComPapelDefinidoAcessaApenasRecursosDoSeuEscopo() {
        // Arrange
        TenantAccessControlService service = new TenantAccessControlService();

        // Act
        RecursoDoTenant recurso = service.acessarRecurso("companhia-A", "recurso-1", "companhia-A");

        // Assert
        assertEquals("companhia-A", recurso.tenantDonoId());
    }

    @Test
    void rnH03_proib_companhiaAAcessaDadosDaCompanhiaBEBloqueado() {
        // Arrange
        TenantAccessControlService service = new TenantAccessControlService();

        // Act & Assert: 0% de vazamento é a meta (RNF-27) — tentativa cross-tenant deve ser bloqueada
        assertThrows(CrossTenantAccessDeniedException.class,
                () -> service.acessarRecurso("companhia-A", "recurso-1", "companhia-B"));
    }

    @Test
    @Disabled("CONF: pela fonte-base (RNF-27 literal), o único controle exigido é a auditoria trimestral; "
            + "detecção contínua de vazamento entre janelas de auditoria é apenas proposta do ADR-04, não regra "
            + "adotada (ver Seção 3, Q-06 do plano-tdd.md). Teste de detecção em tempo real não pode ser escrito "
            + "como requisito de aceite hoje.")
    void rnH03_conf_auditoriaTrimestralNaoDetectaVazamentoEntreJanelas_pendenteDeAdocaoDoAdr04() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
