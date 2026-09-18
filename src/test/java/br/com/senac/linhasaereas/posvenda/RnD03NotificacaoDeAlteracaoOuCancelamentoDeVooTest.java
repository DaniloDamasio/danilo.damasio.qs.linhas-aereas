package br.com.senac.linhasaereas.posvenda;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-D03 — Notificação automática de alteração/cancelamento de voo pela companhia.
 * Origem: RF-18.
 *
 * Nota: a linha "Gap/Conflito de atribuição" (origem declarada de RF-18 — C-01, plano-tdd.md
 * §3.1) é apenas documental (não possui Tipo CF/LIM/INV/CONF/PROIB) e não gera caso de teste
 * JUnit — registrada como N/A no relatório de execução.
 */
class RnD03NotificacaoDeAlteracaoOuCancelamentoDeVooTest {

    @Test
    void rnD03_cf_companhiaCancelaVooPassageiroEAgenteB2bSaoNotificados() {
        // Arrange: evento de cancelamento recebido do Adaptador de Integração, reserva via agente B2B
        FlightChangeNotificationService service = new FlightChangeNotificationService();
        FlightChangeEvent evento = new FlightChangeEvent("VOO-300", FlightEventType.CANCELLED, Instant.now());

        // Act
        NotificationRecipients recipientes = service.notify(evento, ReservationChannel.B2B_AGENT);

        // Assert: passageiro notificado automaticamente; agente B2B também notificado
        assertTrue(recipientes.passengerNotified());
        assertTrue(recipientes.agentNotified());
    }

    @Test
    void rnD03_conf_vooAlteradoComMudancaDeHorarioTambemGeraNotificacao() {
        // Arrange: evento de alteração de horário (não cancelamento), reserva direta
        FlightChangeNotificationService service = new FlightChangeNotificationService();
        FlightChangeEvent evento = new FlightChangeEvent("VOO-301", FlightEventType.RESCHEDULED, Instant.now());

        // Act
        NotificationRecipients recipientes = service.notify(evento, ReservationChannel.DIRECT);

        // Assert: RF-18 cobre "alteração ou cancelamento" — notificação também deve ocorrer para alteração
        assertTrue(recipientes.passengerNotified());
    }
}
