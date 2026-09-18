package br.com.senac.linhasaereas.remarcacao;

import java.math.BigDecimal;

/**
 * Indicação de política de remarcação/cancelamento antes da compra — RF-05 (RN-B05).
 */
public class CancellationPolicyService {

    public RemarcacaoPolicy politicaDe(String vooId) {
        return new RemarcacaoPolicy(vooId, true, BigDecimal.ZERO);
    }

    public void confirmarCompra(String vooId, boolean politicaExibidaAoUsuario) {
        if (!politicaExibidaAoUsuario) {
            throw new PolicyNotDisplayedException(
                    "compra do voo " + vooId + " não pode ser concluída sem exibir a política de remarcação antes");
        }
    }
}
