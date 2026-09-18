package br.com.senac.linhasaereas.checkout;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-C08 — Transparência da tarifa e limite de upsell.
 * Origem: RF-14; RNF-15.
 */
class RnC08TransparenciaDaTarifaEUpsellTest {

    @Test
    void rnC08_cf_exibicaoDoQueEstaInclusoAntesDaConfirmacao() {
        // Arrange
        CheckoutUpsellService service = new CheckoutUpsellService();

        // Act
        InformacaoTarifa informacao = service.informacoesInclusas("tarifa-1");

        // Assert: composição da tarifa (bagagem de mão e despachada) deve ser exposta de forma clara
        // antes da confirmação, e não apenas descoberta após a compra
        assertNotNull(informacao, "informação de bagagem incluída deve ser exibida antes da confirmação");
    }

    @Test
    void rnC08_lim_exatamente3OfertasDeUpsellExibidas() {
        // Arrange
        CheckoutUpsellService service = new CheckoutUpsellService();
        List<OfertaUpsell> disponiveis = List.of(
                new OfertaUpsell("Bagagem extra", false),
                new OfertaUpsell("Assento premium", false),
                new OfertaUpsell("Seguro viagem", false)
        );

        // Act
        List<OfertaUpsell> exibidas = service.ofertasExibidas(disponiveis);

        // Assert: 3 ofertas é o limite máximo permitido, ainda conforme
        assertEquals(3, exibidas.size());
    }

    @Test
    void rnC08_inv_maisDe3OfertasDeUpsellExibidasPorPadraoNaoEConforme() {
        // Arrange
        CheckoutUpsellService service = new CheckoutUpsellService();
        List<OfertaUpsell> disponiveis = List.of(
                new OfertaUpsell("Bagagem extra", false),
                new OfertaUpsell("Assento premium", false),
                new OfertaUpsell("Seguro viagem", false),
                new OfertaUpsell("Prioridade de embarque", false)
        );

        // Act
        List<OfertaUpsell> exibidas = service.ofertasExibidas(disponiveis);

        // Assert: deve limitar a 3 por padrão, mesmo havendo 4+ configuradas
        assertTrue(exibidas.size() <= 3, "no máximo 3 ofertas de upsell devem ser exibidas por padrão");
    }

    @Test
    void rnC08_proib_upsellPreMarcadoPreSelecionadoEProibido() {
        // Arrange
        CheckoutUpsellService service = new CheckoutUpsellService();
        List<OfertaUpsell> disponiveis = List.of(new OfertaUpsell("Bagagem extra", true));

        // Act
        List<OfertaUpsell> exibidas = service.ofertasExibidas(disponiveis);

        // Assert: nenhuma oferta pode vir pré-marcada; exige opt-in explícito
        assertTrue(exibidas.stream().noneMatch(OfertaUpsell::preSelecionada),
                "nenhuma oferta de upsell pode vir pré-selecionada por padrão");
    }

    @Test
    void rnC08_cf_ocultarTodasAsOfertasEmUmCliqueRemoveTodasImediatamente() {
        // Arrange
        CheckoutUpsellService service = new CheckoutUpsellService();
        List<OfertaUpsell> exibidas = List.of(
                new OfertaUpsell("Bagagem extra", false),
                new OfertaUpsell("Assento premium", false)
        );

        // Act
        List<OfertaUpsell> resultado = service.ocultarTodas(exibidas);

        // Assert
        assertTrue(resultado.isEmpty(), "acionar 'ocultar todas' deve remover todas as ofertas imediatamente");
    }
}
