package br.com.senac.linhasaereas.seguranca;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-H02 — Exclusão de dados pessoais (LGPD) vs. retenção obrigatória de auditoria/fiscal.
 * Origem: RNF-25 (exclusão ≤15 dias) vs. RNF-23/RNF-28 (retenção ≥5 anos); ADR-09 [proposta, não decisão].
 */
class RnH02ExclusaoLgpdTest {

    @Test
    void rnH02_cf_titularSemTransacoesPendentesDeAuditoriaEExcluidoEmAte15DiasCorridos() {
        // Arrange
        PersonalDataErasureService service = new PersonalDataErasureService();
        TitularDados titular = new TitularDados("titular-1", false, false);

        // Act
        ResultadoExclusaoLgpd resultado = service.solicitarExclusao(titular);

        // Assert
        assertEquals(StatusExclusaoLgpd.EXCLUIDO, resultado.status());
        assertTrue(resultado.prazoParaConclusao().compareTo(Duration.ofDays(15)) <= 0,
                "exclusão sem vínculo com obrigação legal ativa deve ocorrer em até 15 dias corridos");
    }

    @Test
    void rnH02_conf_titularComTransacaoEmRetencaoDeAuditoriaTemExclusaoTotalDentroDoPrazo() {
        // Arrange
        PersonalDataErasureService service = new PersonalDataErasureService();
        TitularDados titularComRetencaoDeAuditoria = new TitularDados("titular-2", false, true);

        // Act
        ResultadoExclusaoLgpd resultado = service.solicitarExclusao(titularComRetencaoDeAuditoria);

        // Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 7) —
        // a exclusão prevalece sobre a retenção de auditoria/fiscal; apaga-se tudo, inclusive a trilha
        // financeira, dentro do prazo de RNF-25 (<=15 dias corridos). Rejeita a pseudonimização proposta
        // pelo ADR-09.
        assertEquals(StatusExclusaoLgpd.EXCLUIDO, resultado.status());
        assertTrue(resultado.trilhaFinanceiraExcluida(),
                "decisão do stakeholder (2026-09-17): exclusão apaga inclusive a trilha financeira de auditoria");
        assertTrue(resultado.prazoParaConclusao().compareTo(Duration.ofDays(15)) <= 0,
                "exclusão mesmo com retenção de auditoria ativa deve ocorrer em até 15 dias corridos");
    }

    @Test
    void rnH02_cf_dadoDeMenorDeIdadeSujeitoAExclusaoRecebeMesmoTratamentoDeRnf25() {
        // Arrange
        PersonalDataErasureService service = new PersonalDataErasureService();
        TitularDados menor = new TitularDados("titular-menor-1", true, false);

        // Act
        ResultadoExclusaoLgpd resultado = service.solicitarExclusao(menor);

        // Assert: fonte não define tratamento diferenciado além da exigência geral de LGPD
        assertEquals(StatusExclusaoLgpd.EXCLUIDO, resultado.status());
        assertTrue(resultado.prazoParaConclusao().compareTo(Duration.ofDays(15)) <= 0,
                "dado de menor sem vínculo com obrigação legal ativa segue o mesmo prazo de RNF-25");
    }
}
