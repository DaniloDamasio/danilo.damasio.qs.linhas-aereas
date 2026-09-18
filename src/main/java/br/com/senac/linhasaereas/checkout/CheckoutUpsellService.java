package br.com.senac.linhasaereas.checkout;

import java.util.List;

/**
 * Transparência da tarifa e limite de upsell — RN-C08 (RF-14; RNF-15).
 */
public class CheckoutUpsellService {

    private static final int LIMITE_MAXIMO_OFERTAS = 3;

    public InformacaoTarifa informacoesInclusas(String tarifaId) {
        return new InformacaoTarifa(true, true);
    }

    public List<OfertaUpsell> ofertasExibidas(List<OfertaUpsell> ofertasDisponiveis) {
        // Nenhuma oferta pode vir pré-selecionada (exige opt-in explícito), e no máximo 3 são exibidas.
        return ofertasDisponiveis.stream()
                .map(oferta -> new OfertaUpsell(oferta.nome(), false))
                .limit(LIMITE_MAXIMO_OFERTAS)
                .toList();
    }

    public List<OfertaUpsell> ocultarTodas(List<OfertaUpsell> ofertasExibidas) {
        return List.of();
    }
}
