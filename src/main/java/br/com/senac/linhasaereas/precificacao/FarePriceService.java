package br.com.senac.linhasaereas.precificacao;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Preço final sem custo oculto e congelamento de sessão — RF-03; RNF-01; RNF-14; RF-14 (RN-B03).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class FarePriceService {

    public BigDecimal precoNaBusca(String vooId, String sessaoId) {
        throw new UnsupportedOperationException("preço exibido na busca ainda não implementado (RN-B03)");
    }

    public BigDecimal precoNoCheckout(String vooId, String sessaoId) {
        throw new UnsupportedOperationException("preço no checkout ainda não implementado (RN-B03)");
    }

    public PrecoComposto composicaoDoPreco(String vooId) {
        throw new UnsupportedOperationException("composição do preço final ainda não implementada (RN-B03)");
    }

    public void atualizarTarifaDinamica(String vooId, BigDecimal novoPreco, Instant momento) {
        throw new UnsupportedOperationException(
                "atualização de tarifa dinâmica entre sessões ainda não implementada (RN-B03)");
    }

    public PrecoComSinalizacao precoNoCheckoutComSinalizacaoDeAlteracao(String vooId, String sessaoId) {
        throw new UnsupportedOperationException(
                "sinalização de alteração de tarifa dinâmica entre sessões ainda não implementada (RN-B03)");
    }
}
