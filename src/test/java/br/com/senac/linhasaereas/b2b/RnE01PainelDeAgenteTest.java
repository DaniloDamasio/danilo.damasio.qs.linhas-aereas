package br.com.senac.linhasaereas.b2b;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-E01 — Painel de agente operando em nome de terceiros.
 * Origem: RF-19; RNF-27.
 */
class RnE01PainelDeAgenteTest {

    private static final String AGENTE = "agente-1";
    private static final String EMPRESA_ATIVA = "empresa-acme";
    private static final String EMPRESA_DIFERENTE = "empresa-globex";
    private static final String FUNCIONARIO = "funcionario-joao";

    @Test
    void rnE01_cf_agenteSelecionaEmpresaClienteEReservaEmNomeDeFuncionario() {
        // Arrange
        CorporateAgentPanelService service = new CorporateAgentPanelService();

        // Act
        CorporateBooking reserva = service.bookOnBehalfOf(AGENTE, EMPRESA_ATIVA, FUNCIONARIO, "VOO-200", "10A");

        // Assert
        assertEquals(EMPRESA_ATIVA, reserva.empresaClienteId());
        assertEquals(FUNCIONARIO, reserva.funcionarioId());
    }

    @Test
    void rnE01_proib_agenteAcessaDadosDeEmpresaClienteDiferente() {
        // Arrange
        CorporateAgentPanelService service = new CorporateAgentPanelService();

        // Act & Assert: tentativa de acesso cross-tenant deve ser bloqueada (RNF-27: 0% de vazamento)
        assertThrows(CrossTenantAccessException.class,
                () -> service.listBookingsForCompany(AGENTE, EMPRESA_DIFERENTE),
                "agente com contexto ativo em outra empresa não pode acessar dados/reservas de empresa cliente diferente");
    }
}
