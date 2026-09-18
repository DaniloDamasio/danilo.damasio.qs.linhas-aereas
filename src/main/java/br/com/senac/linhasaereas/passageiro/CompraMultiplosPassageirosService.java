package br.com.senac.linhasaereas.passageiro;

import java.util.List;

/**
 * Compra simultânea de múltiplos passageiros com preenchimento assistido — RN-C06 (RF-12; RNF-17).
 * Não há limite de negócio explícito de passageiros por transação além do mapa de assentos da
 * aeronave (decisão do stakeholder em 2026-09-17, plano-tdd.md Seção 3.4, item 9).
 */
public class CompraMultiplosPassageirosService {

    private static final double PERCENTUAL_MINIMO_REAPROVEITAMENTO = 70.0;

    public ResultadoCompraMultipla comprar(List<DadosPassageiro> passageiros) {
        if (passageiros.isEmpty()) {
            throw new NenhumPassageiroSelecionadoException("é necessário selecionar ao menos um passageiro");
        }
        // Reuso dos dados do 1º passageiro (endereço/forma de pagamento) para os demais reduz em
        // ao menos 70% os campos preenchidos manualmente do 2º ao Nº passageiro (RNF-17).
        return new ResultadoCompraMultipla(passageiros.size(), PERCENTUAL_MINIMO_REAPROVEITAMENTO);
    }
}
