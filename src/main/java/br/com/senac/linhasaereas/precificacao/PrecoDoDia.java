package br.com.senac.linhasaereas.precificacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entrada do calendário de preços para um dia específico — RF-02 (RN-B02).
 * Dado simples, sem regra de negócio. {@code preco} pode ser {@code null} quando
 * o dia não tem voo disponível; {@code disponivel} indica se há voo cadastrado nesse dia.
 */
public record PrecoDoDia(LocalDate data, BigDecimal preco, boolean disponivel) {
}
