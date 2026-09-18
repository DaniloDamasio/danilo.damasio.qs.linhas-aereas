package br.com.senac.linhasaereas.seguranca;

import java.time.Duration;
import java.time.Instant;

/** Registro de auditoria de uma chamada de API autenticada (RNF-28, RN-H04). */
public record AuditRecord(String chamadaId, Instant registradoEm, Duration prazoDeRetencao) {
}
