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

    public Optional<AnomalyAlert> avaliarAnomalia(RefundVolumeSample amostra) {
        throw new UnsupportedOperationException(
                "detecção de anomalia de volume de reembolsos ainda não implementada (RN-G03)");
    }

    public boolean dentroDoSlaDeQuinzeMinutos(Instant detectadoEm, Instant disparadoEm) {
        throw new UnsupportedOperationException(
                "verificação de SLA de disparo do alerta de anomalia ainda não implementada (RN-G03)");
    }
}
