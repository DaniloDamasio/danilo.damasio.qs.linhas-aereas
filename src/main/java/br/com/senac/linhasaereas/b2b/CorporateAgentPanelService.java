package br.com.senac.linhasaereas.b2b;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Painel de agente de viagens corporativas operando em nome de terceiros (RN-E01).
 * Origem: RF-19; RNF-27.
 */
public class CorporateAgentPanelService {

    private final Map<String, String> empresaAtivaPorAgente = new ConcurrentHashMap<>();
    private final Map<String, List<CorporateBooking>> reservasPorEmpresa = new ConcurrentHashMap<>();

    public CorporateBooking bookOnBehalfOf(String agentId, String empresaClienteAtivaId, String funcionarioId,
                                            String vooId, String assentoId) {
        empresaAtivaPorAgente.put(agentId, empresaClienteAtivaId);
        CorporateBooking reserva = new CorporateBooking(
                UUID.randomUUID().toString(), empresaClienteAtivaId, funcionarioId, vooId, assentoId);
        reservasPorEmpresa
                .computeIfAbsent(empresaClienteAtivaId, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(reserva);
        return reserva;
    }

    public List<CorporateBooking> listBookingsForCompany(String agentId, String empresaClienteId) {
        String empresaAtiva = empresaAtivaPorAgente.get(agentId);
        if (empresaAtiva == null || !empresaAtiva.equals(empresaClienteId)) {
            throw new CrossTenantAccessException(
                    "agente " + agentId + " não pode acessar dados da empresa cliente " + empresaClienteId);
        }
        return List.copyOf(reservasPorEmpresa.getOrDefault(empresaClienteId, List.of()));
    }
}
