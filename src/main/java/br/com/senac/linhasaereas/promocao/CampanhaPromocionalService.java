package br.com.senac.linhasaereas.promocao;

/**
 * Campanhas promocionais com quantidade de assentos promocionais — RN-F02 (RF-25).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class CampanhaPromocionalService {

    public CampanhaPromocional criar(CampanhaPromocionalRequest request) {
        throw new UnsupportedOperationException("criação de campanha promocional ainda não implementada (RN-F02)");
    }

    public void venderAssentoPromocional(String campanhaId) {
        throw new UnsupportedOperationException(
                "venda de assento promocional ainda não implementada (RN-F02)");
    }

    public boolean contagemPromocionalDivergeDoEstoqueReal(String campanhaId) {
        throw new UnsupportedOperationException(
                "verificação de divergência entre contagem promocional e estoque real ainda não implementada (RN-F02)");
    }
}
