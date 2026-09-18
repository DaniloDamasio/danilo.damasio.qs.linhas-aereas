package br.com.senac.linhasaereas.painelsaude;

/**
 * Painel único de indicadores de saúde da operação — RF-30 (RN-G02).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class OperationalHealthDashboardService {

    public HealthIndicators indicadoresConsolidados(String companhiaId) {
        return new HealthIndicators(companhiaId, 0, 0, 0);
    }

    public HealthIndicators indicadoresNoContextoDe(String companhiaSolicitanteId, String companhiaAlvoId) {
        return indicadoresConsolidados(companhiaSolicitanteId);
    }
}
