package br.com.senac.linhasaereas.notificacao;

/**
 * Reserva confirmada, alvo de notificação de confirmação e localizador — RN-C04 (RF-10; RNF-11).
 * Classe de dados simples, sem regra de negócio.
 */
public record Reserva(String reservaId, String localizador) {
}
