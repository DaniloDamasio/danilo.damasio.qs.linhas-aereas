package br.com.senac.linhasaereas.notificacao;

import java.time.Duration;

/**
 * Resultado do envio de confirmação e localizador — RN-C04 (RF-10; RNF-11).
 * Classe de dados simples, sem regra de negócio.
 */
public record EnvioResultado(boolean emailEnviado, boolean whatsappEnviado, Duration duracao) {
}
