package br.com.senac.linhasaereas.precificacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calendário de preços (±15 dias mínimo) — RF-02 (RN-B02).
 */
public class PriceCalendarService {

    private static final int JANELA_MINIMA_DIAS = 15;
    private static final BigDecimal PRECO_BASE = new BigDecimal("500.00");

    public Map<LocalDate, PrecoDoDia> calendario(LocalDate dataCentral) {
        Map<LocalDate, PrecoDoDia> calendario = new LinkedHashMap<>();
        for (int offset = -JANELA_MINIMA_DIAS; offset <= JANELA_MINIMA_DIAS; offset++) {
            LocalDate data = dataCentral.plusDays(offset);
            calendario.put(data, new PrecoDoDia(data, PRECO_BASE, true));
        }
        return calendario;
    }
}
