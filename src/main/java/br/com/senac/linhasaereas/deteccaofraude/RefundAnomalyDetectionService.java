package br.com.senac.linhasaereas.deteccaofraude;

import java.time.Instant;
import java.util.Optional;

/**
 * Alertas automáticos de padrões anômalos de reembolso (fraude) — RF-31; RNF-26 (RN-G03).
 * Regra quantitativa da fonte: anomalia = volume de reembolsos ≥ 3× a média móvel dos últimos 7 dias,
 * para a mesma rota/companhia; alerta deve disparar em ≤ 15 minutos após a detecção.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class RefundAnomalyDetectionService {

    private static final double LIMIAR_MULTIPLICADOR = 3.0;
    private static final java.time.Duration SLA_ALERTA = java.time.Duration.ofMinutes(15);

    public Optional<AnomalyAlert> avaliarAnomalia(RefundVolumeSample amostra) {
        double limiar = amostra.mediaMovelSeteDias() * LIMIAR_MULTIPLICADOR;
        if (amostra.volumeReembolsos() >= limiar) {
            return Optional.of(new AnomalyAlert(
                    amostra.rotaOuCompanhiaId(), amostra.volumeReembolsos(), limiar, amostra.observadoEm()));
        }
        return Optional.empty();
    }

    public boolean dentroDoSlaDeQuinzeMinutos(Instant detectadoEm, Instant disparadoEm) {
        return java.time.Duration.between(detectadoEm, disparadoEm).compareTo(SLA_ALERTA) <= 0;
    }
}
