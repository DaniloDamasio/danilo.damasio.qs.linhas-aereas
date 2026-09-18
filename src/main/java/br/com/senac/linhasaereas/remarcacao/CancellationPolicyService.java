package br.com.senac.linhasaereas.remarcacao;

/**
 * Indicação de política de remarcação/cancelamento antes da compra — RF-05 (RN-B05).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class CancellationPolicyService {

    public RemarcacaoPolicy politicaDe(String vooId) {
        throw new UnsupportedOperationException("política de remarcação/cancelamento ainda não implementada (RN-B05)");
    }

    public void confirmarCompra(String vooId, boolean politicaExibidaAoUsuario) {
        throw new UnsupportedOperationException("confirmação de compra com verificação de política ainda não implementada (RN-B05)");
    }
}
