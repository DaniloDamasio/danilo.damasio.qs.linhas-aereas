package br.com.senac.linhasaereas.conciliacao;

import java.util.List;

/**
 * Conciliação financeira idempotente — RF-32; RNF-23 (RN-G04).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class FinancialReconciliationService {

    public ReconciliationReport reconciliar(String periodo, List<FinancialEvent> eventos) {
        throw new UnsupportedOperationException(
                "conciliação financeira idempotente ainda não implementada (RN-G04)");
    }

    public List<AuditEntry> trilhaDeAuditoria(String periodo) {
        throw new UnsupportedOperationException(
                "trilha de auditoria imutável de conciliação ainda não implementada (RN-G04)");
    }
}
