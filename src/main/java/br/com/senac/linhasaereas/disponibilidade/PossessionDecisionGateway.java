package br.com.senac.linhasaereas.disponibilidade;

import br.com.senac.linhasaereas.estoque.SeatHold;
import br.com.senac.linhasaereas.estoque.SeatStatus;

import java.time.Instant;

/**
 * Ponto único de decisão de posse — comportamento fail-closed quando o Estoque está
 * indisponível (arquitetura.md §0, §12; RESTRIÇÃO-CRÍTICA-01; RN-I02).
 */
public class PossessionDecisionGateway {

    public SeatHold confirmarPosse(String vooId, String assentoId, boolean estoqueDisponivel) {
        if (!estoqueDisponivel) {
            throw new StockUnavailableException(
                    "Estoque indisponível — decisão de posse recusada (fail-closed, RESTRIÇÃO-CRÍTICA-01)");
        }
        return new SeatHold("posse-" + vooId + "-" + assentoId, vooId, assentoId, "sessao-desconhecida",
                SeatStatus.CONFIRMED, Instant.now());
    }

    public boolean buscarComFailClosed(String vooId, boolean estoqueDisponivel) {
        if (!estoqueDisponivel) {
            throw new StockUnavailableException(
                    "Estoque indisponível — busca recusada (fail-closed total, decisão do stakeholder "
                            + "em 2026-09-17, plano-tdd.md Seção 3.4, item 6)");
        }
        return true;
    }
}
