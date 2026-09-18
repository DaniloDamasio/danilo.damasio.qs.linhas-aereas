package br.com.senac.linhasaereas.b2b;

import java.util.List;

/**
 * Motor de políticas de viagem por empresa cliente (RN-E02).
 * Origem: RF-20; RNF-02; RNF-30; ADR-08 [proposta, não decisão].
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class TravelPolicyEngineService {

    public List<FlightOffer> searchWithinPolicy(String empresaClienteId, List<FlightOffer> ofertasDisponiveis) {
        throw new UnsupportedOperationException(
                "sugestão automática de voos dentro da política ainda não implementada (RN-E02)");
    }

    public CorporateBooking bookIfWithinPolicy(String empresaClienteId, FlightOffer ofertaEscolhida,
                                                String funcionarioId) {
        throw new UnsupportedOperationException(
                "bloqueio/sinalização de reserva fora da política ainda não implementado (RN-E02)");
    }

    public void updatePolicy(String empresaClienteId, TravelPolicy novaPolitica) {
        throw new UnsupportedOperationException(
                "atualização de política sem deploy ainda não implementada (RN-E02)");
    }

    public TravelPolicy getActivePolicy(String empresaClienteId) {
        throw new UnsupportedOperationException(
                "consulta de política ativa ainda não implementada (RN-E02)");
    }
}
