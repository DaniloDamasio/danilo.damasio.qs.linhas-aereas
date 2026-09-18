package br.com.senac.linhasaereas.precificacao;

import java.math.BigDecimal;

/**
 * Preço no checkout acompanhado da sinalização de eventual alteração de tarifa dinâmica
 * ocorrida entre sessões distintas (RN-B03; decisão do stakeholder em 2026-09-17, plano-tdd.md
 * Seção 3.4, item 2 — ADR-07 adotado).
 */
public record PrecoComSinalizacao(BigDecimal preco, boolean tarifaAlteradaDesdeAUltimaSessao) {
}
