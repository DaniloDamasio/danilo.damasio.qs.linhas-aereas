package br.com.senac.linhasaereas.concorrencia;

import java.util.List;

/**
 * Resultado do ranking de exibição/preço de uma rota frente à concorrência — RN-F05 (RF-28).
 * A metodologia de cálculo do ranking não é definida pela fonte (gap — plano-tdd.md Seção 3.2);
 * este tipo apenas transporta o resultado exibido, sem impor um algoritmo.
 */
public record RankingResultado(
        String rotaId,
        List<String> posicoesPorCompanhia
) {
}
