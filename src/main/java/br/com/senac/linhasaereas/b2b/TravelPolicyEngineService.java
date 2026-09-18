package br.com.senac.linhasaereas.b2b;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Motor de políticas de viagem por empresa cliente (RN-E02).
 * Origem: RF-20; RNF-02; RNF-30; ADR-08 [proposta, não decisão].
 *
 * Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md §3.4, item 3): 100% das reservas
 * fora de política devem ser bloqueadas ou sinalizadas — meta rígida de critério de aceite, sem a
 * banda de tolerância de 0,1%/mês proposta (e rejeitada) pelo ADR-08.
 */
public class TravelPolicyEngineService {

    private final Map<String, TravelPolicy> politicasAtivas = new ConcurrentHashMap<>();

    public List<FlightOffer> searchWithinPolicy(String empresaClienteId, List<FlightOffer> ofertasDisponiveis) {
        return ofertasDisponiveis.stream()
                .filter(oferta -> isWithinPolicy(empresaClienteId, oferta))
                .collect(Collectors.toList());
    }

    public CorporateBooking bookIfWithinPolicy(String empresaClienteId, FlightOffer ofertaEscolhida,
                                                String funcionarioId) {
        if (!isWithinPolicy(empresaClienteId, ofertaEscolhida)) {
            throw new PolicyViolationException(
                    "oferta " + ofertaEscolhida.vooId() + " está fora da política de viagem da empresa "
                            + empresaClienteId);
        }
        return new CorporateBooking(UUID.randomUUID().toString(), empresaClienteId, funcionarioId,
                ofertaEscolhida.vooId(), null);
    }

    public void updatePolicy(String empresaClienteId, TravelPolicy novaPolitica) {
        politicasAtivas.put(empresaClienteId, novaPolitica);
    }

    public TravelPolicy getActivePolicy(String empresaClienteId) {
        return politicasAtivas.get(empresaClienteId);
    }

    private static final String CLASSE_PADRAO_SEM_POLITICA_CONFIGURADA = "ECONOMICA";

    private boolean isWithinPolicy(String empresaClienteId, FlightOffer oferta) {
        TravelPolicy politica = politicasAtivas.get(empresaClienteId);
        if (politica == null) {
            return CLASSE_PADRAO_SEM_POLITICA_CONFIGURADA.equals(oferta.classe());
        }
        boolean classeOk = politica.classePermitida().equals(oferta.classe());
        boolean precoOk = oferta.preco() <= politica.tetoValorReais();
        boolean companhiaOk = politica.companhiasCredenciadas().isEmpty()
                || politica.companhiasCredenciadas().contains(oferta.companhiaAerea());
        return classeOk && precoOk && companhiaOk;
    }
}
