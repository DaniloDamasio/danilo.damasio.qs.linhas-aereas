package br.com.senac.linhasaereas.posvenda;

import br.com.senac.linhasaereas.estoque.SeatUnavailableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-D02 — Remarcação e cancelamento self-service.
 * Origem: RF-17.
 */
class RnD02RemarcacaoECancelamentoSelfServiceTest {

    @Test
    void rnD02_cf_remarcacaoSemCustoDentroDaPoliticaDaTarifa() {
        // Arrange: reserva com tarifa flexível
        PostSaleChangeService service = new PostSaleChangeService();

        // Act
        ChangeQuote cotacao = service.quoteRebooking("reserva-1", "VOO-200", "12A");
        ChangeResult resultado = service.confirmChange(cotacao.quoteId());

        // Assert: alteração concluída sem cobrança
        assertEquals(BigDecimal.ZERO, cotacao.cost());
        assertEquals(BigDecimal.ZERO, resultado.costCharged());
    }

    @Test
    void rnD02_cf_cancelamentoComMultaExibidaEConfirmadaAntesDaEfetivacao() {
        // Arrange: reserva com tarifa restritiva
        PostSaleChangeService service = new PostSaleChangeService();

        // Act
        ChangeQuote cotacao = service.quoteCancellation("reserva-2");
        ChangeResult resultado = service.confirmChange(cotacao.quoteId());

        // Assert: custo exibido na cotação é o mesmo cobrado na efetivação
        assertTrue(cotacao.cost().compareTo(BigDecimal.ZERO) > 0, "multa deve ser exibida antes da efetivação");
        assertEquals(cotacao.cost(), resultado.costCharged());
    }

    @Test
    void rnD02_proib_alteracaoEfetivadaSemExibirCustoAntesDaConfirmacaoERejeitada() {
        // Arrange: fluxo tenta confirmar uma alteração sem ter passado pela etapa de cotação
        PostSaleChangeService service = new PostSaleChangeService();

        // Act & Assert: RF-17 exige exibição de custo/prazo antes da confirmação da alteração
        assertThrows(ChangeQuoteRequiredException.class,
                () -> service.confirmChange("quote-inexistente"));
    }

    @Test
    void rnD02_conf_remarcacaoParaVooSemAssentosDisponiveisEBloqueadaPelaExclusividadeDeAssento() {
        // Arrange: voo de destino sem assentos livres (RN-A01)
        PostSaleChangeService service = new PostSaleChangeService();

        // Act & Assert: bloqueado pela mesma regra de exclusividade de assento (RN-A01)
        assertThrows(SeatUnavailableException.class,
                () -> service.quoteRebooking("reserva-3", "VOO-LOTADO", "12A"));
    }
}
