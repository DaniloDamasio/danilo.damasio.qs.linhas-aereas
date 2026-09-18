package br.com.senac.linhasaereas.integracao;

/**
 * Lançada quando a sincronização de estoque com uma companhia aérea permanece desconhecida após
 * esgotar as tentativas de webhook — fail-closed total (RN-A05, RN-I02; decisão do stakeholder
 * em 2026-09-17, plano-tdd.md Seção 3.4, item 6).
 */
public class AirlineSyncUnknownException extends RuntimeException {
    public AirlineSyncUnknownException(String message) {
        super(message);
    }
}
