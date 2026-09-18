package br.com.senac.linhasaereas.promocao;

import java.time.LocalDate;

/**
 * Campanha promocional de uma rota — RN-F02 (RF-25).
 * A quantidade de assentos promocionais não pode divergir do estoque real
 * (RESTRIÇÃO-CRÍTICA-01).
 */
public record CampanhaPromocional(
        String campanhaId,
        String rotaId,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        int quantidadeAssentosPromocionais,
        boolean ativa
) {
}
