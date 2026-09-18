package br.com.senac.linhasaereas.conciliacao;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Conciliação financeira idempotente — RF-32; RNF-23 (RN-G04).
 */
public class FinancialReconciliationService {

    private static final int RETENCAO_ANOS_MINIMA = 5;

    private final Map<String, Lancamento> lancamentosPorChave = new ConcurrentHashMap<>();
    private final Map<String, List<String>> chavesPorPeriodo = new ConcurrentHashMap<>();

    public ReconciliationReport reconciliar(String periodo, List<FinancialEvent> eventos) {
        for (FinancialEvent evento : eventos) {
            String chave = chaveIdempotencia(periodo, evento);
            lancamentosPorChave.computeIfAbsent(chave,
                    k -> new Lancamento(evento.eventoId(), chave, evento.valor()));
            chavesPorPeriodo.computeIfAbsent(periodo, k -> new java.util.concurrent.CopyOnWriteArrayList<>());
            List<String> chaves = chavesPorPeriodo.get(periodo);
            if (!chaves.contains(chave)) {
                chaves.add(chave);
            }
        }
        List<Lancamento> lancamentos = chavesPorPeriodo.getOrDefault(periodo, List.of()).stream()
                .map(lancamentosPorChave::get)
                .collect(Collectors.toList());
        return new ReconciliationReport(periodo, lancamentos);
    }

    public List<AuditEntry> trilhaDeAuditoria(String periodo) {
        return chavesPorPeriodo.getOrDefault(periodo, List.of()).stream()
                .map(chave -> new AuditEntry(chave, Instant.now(), true, RETENCAO_ANOS_MINIMA))
                .collect(Collectors.toList());
    }

    private String chaveIdempotencia(String periodo, FinancialEvent evento) {
        return periodo + ":" + evento.eventoId();
    }
}
