package br.com.senac.linhasaereas.seguranca;

import java.time.Duration;

/** Resultado de uma solicitação de exclusão de dados pessoais (RNF-25, RN-H02). */
public record ResultadoExclusaoLgpd(
        StatusExclusaoLgpd status,
        Duration prazoParaConclusao,
        boolean trilhaFinanceiraExcluida
) {
}
