package br.com.senac.linhasaereas.b2b;

import java.util.List;

/**
 * Painel de agente de viagens corporativas operando em nome de terceiros (RN-E01).
 * Origem: RF-19; RNF-27.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class CorporateAgentPanelService {

    public CorporateBooking bookOnBehalfOf(String agentId, String empresaClienteAtivaId, String funcionarioId,
                                            String vooId, String assentoId) {
        throw new UnsupportedOperationException(
                "reserva de agente em nome de terceiros ainda não implementada (RN-E01)");
    }

    public List<CorporateBooking> listBookingsForCompany(String agentId, String empresaClienteId) {
        throw new UnsupportedOperationException(
                "consulta de reservas por empresa cliente ainda não implementada (RN-E01)");
    }
}
