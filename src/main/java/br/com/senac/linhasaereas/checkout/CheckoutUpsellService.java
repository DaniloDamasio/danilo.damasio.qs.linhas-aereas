package br.com.senac.linhasaereas.checkout;

import java.util.List;

/**
 * Transparência da tarifa e limite de upsell — RN-C08 (RF-14; RNF-15).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class CheckoutUpsellService {

    public InformacaoTarifa informacoesInclusas(String tarifaId) {
        throw new UnsupportedOperationException("exibição de composição da tarifa ainda não implementada (RN-C08)");
    }

    public List<OfertaUpsell> ofertasExibidas(List<OfertaUpsell> ofertasDisponiveis) {
        throw new UnsupportedOperationException("limite de ofertas de upsell ainda não implementado (RN-C08)");
    }

    public List<OfertaUpsell> ocultarTodas(List<OfertaUpsell> ofertasExibidas) {
        throw new UnsupportedOperationException("ocultar todas as ofertas de upsell ainda não implementado (RN-C08)");
    }
}
