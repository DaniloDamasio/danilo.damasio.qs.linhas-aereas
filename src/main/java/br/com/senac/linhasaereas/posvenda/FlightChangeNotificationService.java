package br.com.senac.linhasaereas.posvenda;

/**
 * Notificação automática de alteração/cancelamento de voo pela companhia — RN-D03.
 * Origem: RF-18.
 */
public class FlightChangeNotificationService {

    public NotificationRecipients notify(FlightChangeEvent event, ReservationChannel channel) {
        boolean agentNotified = channel == ReservationChannel.B2B_AGENT;
        return new NotificationRecipients(true, agentNotified);
    }
}
