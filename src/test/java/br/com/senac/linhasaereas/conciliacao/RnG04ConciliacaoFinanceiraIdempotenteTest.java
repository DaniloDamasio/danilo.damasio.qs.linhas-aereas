package br.com.senac.linhasaereas.conciliacao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-G04 — Conciliação financeira idempotente.
 * Origem: RF-32; RNF-23.
 */
class RnG04ConciliacaoFinanceiraIdempotenteTest {

    private List<FinancialEvent> eventosDoPeriodo(String periodo) {
        return List.of(
                new FinancialEvent("evento-1", "VENDA", 500.00, Instant.parse("2026-08-01T00:00:00Z")),
                new FinancialEvent("evento-2", "REPASSE", 450.00, Instant.parse("2026-08-02T00:00:00Z"))
        );
    }

    @Test
    void rnG04_cf_conciliacaoDeUmPeriodoFechadoGeraRelatorioConsolidadoSemDuplicidade() {
        // Arrange
        FinancialReconciliationService service = new FinancialReconciliationService();
        String periodo = "2026-08";
        List<FinancialEvent> eventos = eventosDoPeriodo(periodo);

        // Act
        ReconciliationReport relatorio = service.reconciliar(periodo, eventos);

        // Assert
        assertEquals(periodo, relatorio.periodo());
        assertEquals(eventos.size(), relatorio.lancamentos().size(),
                "cada evento do período deve gerar exatamente um lançamento, sem duplicidade");
    }

    @Test
    void rnG04_conf_reprocessamentoDoMesmoPeriodoNaoGeraDuplicidadeViaIdempotencia() {
        // Arrange: job de conciliação executado duas vezes para o mesmo período (mesma chave de evento)
        FinancialReconciliationService service = new FinancialReconciliationService();
        String periodo = "2026-08";
        List<FinancialEvent> eventos = eventosDoPeriodo(periodo);

        // Act
        ReconciliationReport primeiraExecucao = service.reconciliar(periodo, eventos);
        ReconciliationReport segundaExecucao = service.reconciliar(periodo, eventos);

        // Assert: 0% de lançamentos duplicados ou perdidos entre as duas execuções
        assertEquals(primeiraExecucao.lancamentos().size(), segundaExecucao.lancamentos().size(),
                "reprocessar o mesmo período não pode alterar a quantidade de lançamentos (idempotência)");
    }

    @Test
    void rnG04_proib_lancamentoDuplicadoAposReprocessamentoNuncaPodeOcorrer() {
        // Arrange: reexecução do job de conciliação para o mesmo evento
        FinancialReconciliationService service = new FinancialReconciliationService();
        String periodo = "2026-08";
        List<FinancialEvent> eventos = eventosDoPeriodo(periodo);
        ReconciliationReport primeiraExecucao = service.reconciliar(periodo, eventos);

        // Act
        ReconciliationReport segundaExecucao = service.reconciliar(periodo, eventos);

        // Assert: nunca pode existir um segundo lançamento para a mesma chave de idempotência (viola RNF-23)
        long chavesUnicas = segundaExecucao.lancamentos().stream()
                .map(Lancamento::chaveIdempotencia)
                .distinct()
                .count();
        assertEquals(primeiraExecucao.lancamentos().size(), chavesUnicas,
                "não pode haver lançamento duplicado para a mesma chave de idempotência após reprocessamento");
    }

    @Test
    void rnG04_cf_trilhaDeAuditoriaDeConciliacaoERegistradaDeFormaImutavelERetidaPorNoMinimoCincoAnos() {
        // Arrange
        FinancialReconciliationService service = new FinancialReconciliationService();
        String periodo = "2026-08";

        // Act
        List<AuditEntry> trilha = service.trilhaDeAuditoria(periodo);

        // Assert
        assertTrue(trilha.stream().allMatch(AuditEntry::imutavel),
                "todo lançamento de conciliação deve ser registrado de forma imutável na trilha de auditoria");
        assertTrue(trilha.stream().allMatch(entrada -> entrada.retencaoAnosMinima() >= 5),
                "trilha de auditoria de conciliação deve ser retida por no mínimo 5 anos");
    }

    @Test
    @Disabled("Conflito entre RNF-23 (retenção ≥ 5 anos da trilha de auditoria) e RNF-25 (exclusão LGPD em "
            + "até 15 dias) quando o titular envolvido pede exclusão dentro do período de retenção obrigatória "
            + "— registrado como conflito A-RSK-10 na Seção 3.1 do plano-tdd.md. A Decisão confirmada pelo "
            + "stakeholder em 2026-09-17 (Seção 3.4, item 7) resolveu RN-H02 (exclusão prevalece, apaga-se "
            + "inclusive a trilha financeira), mas a própria decisão declara explicitamente que isso cria um "
            + "conflito NOVO e NÃO MITIGADO com RNF-23/RF-32 (retenção ≥5 anos) desta RN, que deve ser "
            + "formalmente escalado a quem detém autoridade sobre RNF-23 antes de produção — não deve ser "
            + "tratado como resolução silenciosa. Nenhum critério de aceite definitivo pode ser escrito aqui "
            + "até essa escalada.")
    void rnG04_conf_auditoriaDeConciliacaoVsExclusaoLgpdDeUmTitularEnvolvido_pendenteDeEscalacaoFormal() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
