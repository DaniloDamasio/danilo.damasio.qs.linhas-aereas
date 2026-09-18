package br.com.senac.linhasaereas.integracao;

import br.com.senac.linhasaereas.estoque.SeatHold;

/**
 * Adaptador de Integração — reconciliação de estoque com a companhia aérea (arquitetura.md §9, RN-A05).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class AirlineStockSyncService {

    public SyncResult syncConfirmedSale(SeatHold confirmedSale) {
        throw new UnsupportedOperationException("sincronização de estoque com a companhia ainda não implementada (RN-A05)");
    }

    public void bloquearOperacoesParaCompanhiaComSincronizacaoDesconhecida(String companhiaId) {
        throw new UnsupportedOperationException(
                "fail-closed total após esgotar tentativas de webhook ainda não implementado (RN-A05)");
    }
}
