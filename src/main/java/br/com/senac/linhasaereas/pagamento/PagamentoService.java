package br.com.senac.linhasaereas.pagamento;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Múltiplos meios de pagamento, incluindo Pix e parcelamento — RN-C03 (RF-09; RNF-24).
 */
public class PagamentoService {

    private static final DateTimeFormatter FORMATO_VALIDADE = DateTimeFormatter.ofPattern("MM/yy");

    public ResultadoPagamento pagarViaPix(BigDecimal valor) {
        // Pix é aceito para qualquer valor, inclusive baixo.
        return new ResultadoPagamento(UUID.randomUUID().toString(), StatusPagamento.APROVADO);
    }

    public ResultadoPagamento pagarParcelado(BigDecimal valor, int numeroDeParcelas) {
        return new ResultadoPagamento(UUID.randomUUID().toString(), StatusPagamento.APROVADO);
    }

    public ResultadoPagamento pagarComCartao(DadosCartao cartao, BigDecimal valor) {
        if (!cartaoValido(cartao)) {
            throw new CartaoRecusadoException("cartão recusado pela operadora ou dados inválidos");
        }
        return new ResultadoPagamento(UUID.randomUUID().toString(), StatusPagamento.APROVADO);
    }

    private boolean cartaoValido(DadosCartao cartao) {
        String numero = cartao.numeroCartao();
        if (numero == null || numero.chars().distinct().count() <= 1) {
            // número inválido (ex.: todos os dígitos iguais, como "0000000000000000")
            return false;
        }
        try {
            YearMonth validade = YearMonth.parse(cartao.validade(), FORMATO_VALIDADE);
            if (validade.isBefore(YearMonth.now())) {
                return false;
            }
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    /** Deve retornar uma representação tokenizada do cartão, nunca o PAN em texto claro (RNF-24). */
    public String tokenizar(DadosCartao cartao) {
        return "tok_" + UUID.nameUUIDFromBytes(cartao.numeroCartao().getBytes());
    }
}
