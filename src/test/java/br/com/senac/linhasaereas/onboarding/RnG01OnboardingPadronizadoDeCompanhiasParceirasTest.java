package br.com.senac.linhasaereas.onboarding;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-G01 — Onboarding padronizado de companhias parceiras.
 * Origem: RF-29; RNF-29; ADR-05 [proposta, não decisão].
 */
class RnG01OnboardingPadronizadoDeCompanhiasParceirasTest {

    @Test
    void rnG01_cf_novaCompanhiaCompletaDocumentacaoETestesTecnicosConcluiOnboardingDentroDoBaseline() {
        // Arrange
        PartnerAirlineOnboardingService service = new PartnerAirlineOnboardingService();
        OnboardingApplication solicitacao = new OnboardingApplication(
                "COMPANHIA-01", true, true, Instant.parse("2026-09-01T00:00:00Z"));

        // Act
        OnboardingResult resultado = service.concluirOnboarding(solicitacao);

        // Assert
        assertEquals(OnboardingStatus.CONCLUIDO, resultado.status());
        assertTrue(resultado.duracaoDiasUteis() <= 10,
                "onboarding completo deve concluir em até 10 dias úteis (baseline sugerida S-01)");
    }

    @Test
    @Disabled("ADR-05 propõe segmentação por nível de capacidade técnica declarado (afeta TTL de hold e "
            + "política de fail-closed), mas é proposta, não decisão adotada — não há critério definido de "
            + "aceite/rejeição de parceiro por capacidade técnica (plano-tdd.md, RN-G01, linha CONF).")
    void rnG01_conf_companhiaNaoAtingeNivelDeCapacidadeTecnicaMinima_pendenteDeAdocaoDoAdr05() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
