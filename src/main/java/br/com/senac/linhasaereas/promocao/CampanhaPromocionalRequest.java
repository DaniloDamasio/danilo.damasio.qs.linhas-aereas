package br.com.senac.linhasaereas.promocao;

import java.time.LocalDate;

/**
 * Dados de entrada para criação de campanha promocional — RN-F02 (RF-25).
 */
public record CampanhaPromocionalRequest(
        String rotaId,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        int quantidadeAssentosPromocionais
) {
}
