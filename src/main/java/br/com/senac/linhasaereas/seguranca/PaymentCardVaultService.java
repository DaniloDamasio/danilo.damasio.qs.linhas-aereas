package br.com.senac.linhasaereas.seguranca;

/**
 * Cofre de cartões — tokenização PCI-DSS de dados de pagamento (RNF-24, RN-H01).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PaymentCardVaultService {

    public CardToken tokenize(String panEmTextoClaro) {
        String ultimosQuatro = panEmTextoClaro.substring(panEmTextoClaro.length() - 4);
        String token = "tok_" + java.util.UUID.randomUUID();
        return new CardToken(token, ultimosQuatro);
    }

    public String representacaoPersistida(String token) {
        return "cartao{token=" + token + "}";
    }
}
