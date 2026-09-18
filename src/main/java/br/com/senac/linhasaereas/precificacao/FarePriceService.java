package br.com.senac.linhasaereas.precificacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Preço final sem custo oculto e congelamento de sessão — RF-03; RNF-01; RNF-14; RF-14 (RN-B03).
 * ADR-07 adotado (plano-tdd.md Seção 3.4, item 2): dentro da mesma sessão de checkout o preço
 * é congelado (0% de divergência), independentemente de mudança de tarifa dinâmica no backend;
 * entre sessões distintas a tarifa dinâmica pode variar, desde que sinalizada antes da confirmação.
 */
public class FarePriceService {

    private static final BigDecimal TARIFA_BASE_PADRAO = new BigDecimal("1000.00");
    private static final BigDecimal TAXAS_PADRAO = new BigDecimal("80.00");
    private static final BigDecimal BAGAGEM_PADRAO = new BigDecimal("50.00");
    private static final BigDecimal ENCARGOS_PADRAO = new BigDecimal("20.00");

    private final Map<String, BigDecimal> tarifaDinamicaPorVoo = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> precoCongeladoPorSessao = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> ultimoPrecoConhecidoPorVoo = new ConcurrentHashMap<>();

    public BigDecimal precoNaBusca(String vooId, String sessaoId) {
        BigDecimal precoAtual = tarifaAtual(vooId);
        congelarSessao(vooId, sessaoId, precoAtual);
        return precoAtual;
    }

    public BigDecimal precoNoCheckout(String vooId, String sessaoId) {
        BigDecimal precoCongelado = precoCongeladoPorSessao.get(chave(vooId, sessaoId));
        if (precoCongelado == null) {
            throw new PriceMismatchException(
                    "não há preço congelado para a sessão " + sessaoId
                            + " — checkout sem busca prévia geraria custo oculto");
        }
        return precoCongelado;
    }

    public PrecoComposto composicaoDoPreco(String vooId) {
        BigDecimal tarifaBase = tarifaAtual(vooId);
        BigDecimal total = tarifaBase.add(TAXAS_PADRAO).add(BAGAGEM_PADRAO).add(ENCARGOS_PADRAO);
        return new PrecoComposto(tarifaBase, TAXAS_PADRAO, BAGAGEM_PADRAO, ENCARGOS_PADRAO, total);
    }

    public void atualizarTarifaDinamica(String vooId, BigDecimal novoPreco, Instant momento) {
        tarifaDinamicaPorVoo.put(vooId, novoPreco);
    }

    public PrecoComSinalizacao precoNoCheckoutComSinalizacaoDeAlteracao(String vooId, String sessaoId) {
        BigDecimal ultimoConhecido = ultimoPrecoConhecidoPorVoo.get(vooId);
        BigDecimal precoCongelado = precoCongeladoPorSessao.get(chave(vooId, sessaoId));
        if (precoCongelado == null) {
            precoCongelado = tarifaAtual(vooId);
            congelarSessao(vooId, sessaoId, precoCongelado);
        }
        boolean alterada = ultimoConhecido != null && ultimoConhecido.compareTo(precoCongelado) != 0;
        return new PrecoComSinalizacao(precoCongelado, alterada);
    }

    private BigDecimal tarifaAtual(String vooId) {
        return tarifaDinamicaPorVoo.computeIfAbsent(vooId, id -> TARIFA_BASE_PADRAO);
    }

    private void congelarSessao(String vooId, String sessaoId, BigDecimal preco) {
        precoCongeladoPorSessao.put(chave(vooId, sessaoId), preco);
        ultimoPrecoConhecidoPorVoo.put(vooId, preco);
    }

    private String chave(String vooId, String sessaoId) {
        return Objects.requireNonNull(vooId) + "::" + Objects.requireNonNull(sessaoId);
    }
}
