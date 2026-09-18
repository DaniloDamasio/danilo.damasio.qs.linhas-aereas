package br.com.senac.linhasaereas.passageiro;

import java.util.List;

/**
 * Compra simultânea de múltiplos passageiros com preenchimento assistido — RN-C06 (RF-12; RNF-17).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class CompraMultiplosPassageirosService {

    public ResultadoCompraMultipla comprar(List<DadosPassageiro> passageiros) {
        throw new UnsupportedOperationException("compra com múltiplos passageiros ainda não implementada (RN-C06)");
    }
}
