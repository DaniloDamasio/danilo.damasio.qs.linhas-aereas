package br.com.senac.linhasaereas.precificacao;

import java.math.BigDecimal;

/**
 * Composição do preço final exibido na busca (tarifa + taxas + bagagem + encargos) — RF-03 (RN-B03).
 * Dado simples, sem regra de negócio.
 */
public record PrecoComposto(
        BigDecimal tarifaBase,
        BigDecimal taxas,
        BigDecimal bagagem,
        BigDecimal encargos,
        BigDecimal total
) {
}
