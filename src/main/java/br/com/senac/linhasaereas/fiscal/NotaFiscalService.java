package br.com.senac.linhasaereas.fiscal;

import br.com.senac.linhasaereas.notificacao.Reserva;

import java.time.Duration;

/**
 * Emissão automática de nota fiscal/recibo — RN-C05 (RF-11; RNF-12).
 */
public class NotaFiscalService {

    public NotaFiscal emitir(Reserva reserva) {
        // Emissão automática ao aprovar o pagamento, sem qualquer ação manual do passageiro (RF-11).
        String numero = "NF-" + reserva.localizador();
        return new NotaFiscal(numero, true, true, Duration.ofSeconds(60));
    }
}
