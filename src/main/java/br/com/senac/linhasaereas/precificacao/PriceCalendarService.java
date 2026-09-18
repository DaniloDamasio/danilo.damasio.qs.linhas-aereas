package br.com.senac.linhasaereas.precificacao;

import java.time.LocalDate;
import java.util.Map;

/**
 * Calendário de preços (±15 dias mínimo) — RF-02 (RN-B02).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PriceCalendarService {

    public Map<LocalDate, PrecoDoDia> calendario(LocalDate dataCentral) {
        throw new UnsupportedOperationException("calendário de preços de ±15 dias ainda não implementado (RN-B02)");
    }
}
