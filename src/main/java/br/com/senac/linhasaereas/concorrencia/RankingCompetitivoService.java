package br.com.senac.linhasaereas.concorrencia;

import java.util.List;

/**
 * Posicionamento competitivo (ranking) de exibição/preço por rota — RN-F05 (RF-28).
 * A metodologia de cálculo do ranking não é definida pela fonte (gap — plano-tdd.md Seção 3.2);
 * apenas o caminho feliz de exibição é implementado aqui.
 */
public class RankingCompetitivoService {

    public RankingResultado consultarRanking(RankingConsultaRequest request) {
        return new RankingResultado(request.rotaId(), List.of(request.companhiaId()));
    }
}
