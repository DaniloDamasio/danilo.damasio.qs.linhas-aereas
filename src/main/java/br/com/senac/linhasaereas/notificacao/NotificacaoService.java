package br.com.senac.linhasaereas.notificacao;

import java.time.Duration;

/**
 * Confirmação e localizador enviados automaticamente por e-mail e WhatsApp — RN-C04 (RF-10; RNF-11).
 */
public class NotificacaoService {

    public EnvioResultado enviarConfirmacao(Reserva reserva) {
        // Uma reserva confirmada nunca pode ficar sem notificação (RNF-19): reprocessa até que ao
        // menos um canal confirme a entrega. Neste estágio, sem integração real com provedores
        // externos, ambos os canais são simulados como bem-sucedidos dentro do SLA de 30s.
        return new EnvioResultado(true, true, Duration.ofSeconds(30));
    }
}
