package br.com.senac.linhasaereas.painelsaude;

/**
 * Painel único de indicadores de saúde da operação — RF-30 (RN-G02).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class OperationalHealthDashboardService {

    public HealthIndicators indicadoresConsolidados(String companhiaId) {
        throw new UnsupportedOperationException(
                "indicadores consolidados de saúde da operação ainda não implementados (RN-G02)");
    }

    public HealthIndicators indicadoresNoContextoDe(String companhiaSolicitanteId, String companhiaAlvoId) {
        throw new UnsupportedOperationException(
                "isolamento de indicadores entre companhias no painel administrativo ainda não implementado (RN-G02)");
    }
}
