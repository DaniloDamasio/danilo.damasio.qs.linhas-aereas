package br.com.senac.linhasaereas.posvenda;

/**
 * Notificação automática de alteração/cancelamento de voo pela companhia — RN-D03.
 * Origem: RF-18.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class FlightChangeNotificationService {

    public NotificationRecipients notify(FlightChangeEvent event, ReservationChannel channel) {
        throw new UnsupportedOperationException("notificação automática de alteração/cancelamento de voo ainda não implementada (RN-D03)");
    }
}
