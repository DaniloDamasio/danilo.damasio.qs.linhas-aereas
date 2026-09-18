package br.com.senac.linhasaereas.concorrencia;

/**
 * Filtro para consulta de posicionamento competitivo (ranking) — RN-F05 (RF-28).
 */
public record RankingConsultaRequest(
        String rotaId,
        String companhiaId
) {
}
