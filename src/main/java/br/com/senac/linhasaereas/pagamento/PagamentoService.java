package br.com.senac.linhasaereas.pagamento;

import java.math.BigDecimal;

/**
 * Múltiplos meios de pagamento, incluindo Pix e parcelamento — RN-C03 (RF-09; RNF-24).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PagamentoService {

    public ResultadoPagamento pagarViaPix(BigDecimal valor) {
        throw new UnsupportedOperationException("pagamento via Pix ainda não implementado (RN-C03)");
    }

    public ResultadoPagamento pagarParcelado(BigDecimal valor, int numeroDeParcelas) {
        throw new UnsupportedOperationException("pagamento parcelado ainda não implementado (RN-C03)");
    }

    public ResultadoPagamento pagarComCartao(DadosCartao cartao, BigDecimal valor) {
        throw new UnsupportedOperationException("pagamento com cartão ainda não implementado (RN-C03)");
    }

    /** Deve retornar uma representação tokenizada do cartão, nunca o PAN em texto claro (RNF-24). */
    public String tokenizar(DadosCartao cartao) {
        throw new UnsupportedOperationException("tokenização de cartão ainda não implementada (RN-C03/RNF-24)");
    }
}
