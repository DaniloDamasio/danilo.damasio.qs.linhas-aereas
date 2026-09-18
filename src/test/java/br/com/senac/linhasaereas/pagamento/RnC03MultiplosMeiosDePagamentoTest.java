package br.com.senac.linhasaereas.pagamento;

import br.com.senac.linhasaereas.estoque.SeatHold;
import br.com.senac.linhasaereas.estoque.SeatInventoryService;
import br.com.senac.linhasaereas.estoque.SeatStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-C03 — Múltiplos meios de pagamento, incluindo Pix e parcelamento.
 * Origem: RF-09; RNF-24.
 */
class RnC03MultiplosMeiosDePagamentoTest {

    @Test
    void rnC03_cf_pagamentoViaPix() {
        // Arrange
        PagamentoService service = new PagamentoService();
        BigDecimal valorQualquer = new BigDecimal("15.90");

        // Act
        ResultadoPagamento resultado = service.pagarViaPix(valorQualquer);

        // Assert: aceito mesmo para valores baixos
        assertEquals(StatusPagamento.APROVADO, resultado.status());
    }

    @Test
    void rnC03_cf_pagamentoParceladoNoCartao() {
        // Arrange
        PagamentoService service = new PagamentoService();
        BigDecimal valorAlto = new BigDecimal("4800.00");

        // Act
        ResultadoPagamento resultado = service.pagarParcelado(valorAlto, 8);

        // Assert
        assertEquals(StatusPagamento.APROVADO, resultado.status());
    }

    @Test
    @Disabled("Lacuna: a fonte não quantifica o piso de 'valores baixos' elegíveis a Pix/parcelamento "
            + "(RF-09) — ver plano-tdd.md, Seção 3.2. Não escrever asserção definitiva até essa decisão.")
    void rnC03_lim_parcelamentoDeValorMinimoPermitido_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    void rnC03_inv_cartaoInvalidoOuRecusadoRejeitaTransacaoSemHoldConfirmado() {
        // Arrange
        PagamentoService service = new PagamentoService();
        DadosCartao cartaoRecusado = new DadosCartao("0000000000000000", "01/20", "000");

        // Act & Assert: transação rejeitada; nenhum HOLD deve ser confirmado como consequência (RN-A02)
        assertThrows(CartaoRecusadoException.class,
                () -> service.pagarComCartao(cartaoRecusado, new BigDecimal("300.00")));
    }

    @Test
    void rnC03_proib_armazenamentoDePanEmTextoClaro() {
        // Arrange
        PagamentoService service = new PagamentoService();
        DadosCartao cartao = new DadosCartao("4111111111111111", "12/29", "123");

        // Act
        String tokenArmazenado = service.tokenizar(cartao);

        // Assert: RNF-24 exige 100% dos números tokenizados — o PAN em texto claro nunca pode aparecer no token
        assertFalse(tokenArmazenado.contains(cartao.numeroCartao()),
                "o PAN não pode ser armazenado em texto claro em nenhuma circunstância");
    }

    @Test
    void rnC03_conf_falhaDePagamentoAposHoldConcedidoMantemHoldENaoConfirmaAssento() {
        // Arrange: HOLD ativo sobre um assento antes da tentativa de pagamento
        SeatInventoryService inventario = new SeatInventoryService(Map.of("VOO-300", Set.of("10A")));
        SeatHold hold = inventario.hold("VOO-300", "10A", "sessao-1", Duration.ofMinutes(10));
        PagamentoService pagamentoService = new PagamentoService();
        DadosCartao cartaoRecusado = new DadosCartao("0000000000000000", "01/20", "000");

        // Act & Assert: pagamento recusado
        assertThrows(CartaoRecusadoException.class,
                () -> pagamentoService.pagarComCartao(cartaoRecusado, new BigDecimal("900.00")));

        // Assert: HOLD permanece (não expirou nem foi cancelado) e assento não foi confirmado
        assertEquals(SeatStatus.HOLD, inventario.statusOf("VOO-300", "10A"));
        assertTrue(hold.status() == SeatStatus.HOLD);
    }
}
