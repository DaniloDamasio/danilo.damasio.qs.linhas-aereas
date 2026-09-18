package br.com.senac.linhasaereas.fiscal;

import br.com.senac.linhasaereas.notificacao.Reserva;

/**
 * Emissão automática de nota fiscal/recibo — RN-C05 (RF-11; RNF-12).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class NotaFiscalService {

    public NotaFiscal emitir(Reserva reserva) {
        throw new UnsupportedOperationException("emissão de nota fiscal ainda não implementada (RN-C05)");
    }
}
