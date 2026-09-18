package br.com.senac.linhasaereas.busca;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representação simples de um voo nos resultados de busca (RN-B01/RN-B04).
 * Dado simples, sem regra de negócio.
 */
public record Flight(
        String id,
        String origem,
        String destino,
        Instant partida,
        BigDecimal preco,
        boolean remarcacaoFlexivel
) {
}
