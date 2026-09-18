package br.com.senac.linhasaereas.posvenda;

/**
 * Resultado de quem foi notificado por uma alteração/cancelamento de voo — RN-D03.
 * Se a reserva foi feita via agente B2B, o agente também deve ser notificado (RF-18).
 */
public record NotificationRecipients(
        boolean passengerNotified,
        boolean agentNotified
) {
}
