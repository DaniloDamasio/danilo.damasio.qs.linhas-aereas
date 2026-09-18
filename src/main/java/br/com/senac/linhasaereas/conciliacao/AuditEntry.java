package br.com.senac.linhasaereas.conciliacao;

import java.time.Instant;

/** Registro de trilha de auditoria de um lançamento de conciliação (RN-G04). */
public record AuditEntry(
        String eventoId,
        Instant registradoEm,
        boolean imutavel,
        int retencaoAnosMinima
) {
}
