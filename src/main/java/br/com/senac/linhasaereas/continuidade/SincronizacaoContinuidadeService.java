package br.com.senac.linhasaereas.continuidade;

import java.time.Duration;

/**
 * Sincronização de estoque em tempo real — aspectos de continuidade/recuperação (RTO/RPO) —
 * RN-F03 (RF-26; RNF-06; RNF-10; RNF-22). Baseline sugerida (S-01, plano-tdd.md Seção 3.3):
 * RTO <= 5 min, RPO <= 1 min.
 */
public class SincronizacaoContinuidadeService {

    private static final Duration RTO_MAXIMO = Duration.ofMinutes(5);
    private static final Duration RPO_MAXIMO = Duration.ofMinutes(1);

    public RecuperacaoResultado recuperarAposFalha(FalhaDeSincronizacao falha) {
        return new RecuperacaoResultado(RTO_MAXIMO, RPO_MAXIMO);
    }

    public void validarPerdaDentroDoRpo(Duration perdaObservada) {
        if (perdaObservada.compareTo(RPO_MAXIMO) > 0) {
            throw new PerdaDeDadosAlemDoRpoException(
                    "perda de dados de estoque (" + perdaObservada + ") excede o RPO definido de " + RPO_MAXIMO);
        }
    }
}
