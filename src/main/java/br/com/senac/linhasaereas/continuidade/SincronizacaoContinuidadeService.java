package br.com.senac.linhasaereas.continuidade;

import java.time.Duration;

/**
 * Sincronização de estoque em tempo real — aspectos de continuidade/recuperação (RTO/RPO) —
 * RN-F03 (RF-26; RNF-06; RNF-10; RNF-22).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class SincronizacaoContinuidadeService {

    public RecuperacaoResultado recuperarAposFalha(FalhaDeSincronizacao falha) {
        throw new UnsupportedOperationException("recuperação após falha ainda não implementada (RN-F03)");
    }

    public void validarPerdaDentroDoRpo(Duration perdaObservada) {
        throw new UnsupportedOperationException("validação de perda de dados dentro do RPO ainda não implementada (RN-F03)");
    }
}
