package br.com.senac.linhasaereas.disponibilidade;

import br.com.senac.linhasaereas.estoque.SeatHold;

/**
 * Ponto único de decisão de posse — comportamento fail-closed quando o Estoque está
 * indisponível (arquitetura.md §0, §12; RESTRIÇÃO-CRÍTICA-01; RN-I02).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PossessionDecisionGateway {

    public SeatHold confirmarPosse(String vooId, String assentoId, boolean estoqueDisponivel) {
        throw new UnsupportedOperationException("decisão de posse fail-closed ainda não implementada (RN-I02)");
    }

    public boolean buscarComFailClosed(String vooId, boolean estoqueDisponivel) {
        throw new UnsupportedOperationException(
                "busca fail-closed sob indisponibilidade do Estoque ainda não implementada (RN-I02)");
    }
}
