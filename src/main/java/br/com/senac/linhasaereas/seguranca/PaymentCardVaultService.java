package br.com.senac.linhasaereas.seguranca;

/**
 * Cofre de cartões — tokenização PCI-DSS de dados de pagamento (RNF-24, RN-H01).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PaymentCardVaultService {

    public CardToken tokenize(String panEmTextoClaro) {
        throw new UnsupportedOperationException("tokenização PCI-DSS ainda não implementada (RN-H01)");
    }

    public String representacaoPersistida(String token) {
        throw new UnsupportedOperationException("consulta de representação persistida ainda não implementada (RN-H01)");
    }
}
