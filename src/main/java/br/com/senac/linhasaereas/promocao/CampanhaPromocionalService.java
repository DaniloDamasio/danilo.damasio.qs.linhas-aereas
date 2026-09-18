package br.com.senac.linhasaereas.promocao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Campanhas promocionais com quantidade de assentos promocionais — RN-F02 (RF-25).
 * RESTRIÇÃO-CRÍTICA-01: a contagem promocional não pode divergir do estoque real. O ponto exato
 * de bloqueio quando a demanda excede o lote cadastrado depende de ADR-06 (proposta, não decisão
 * — plano-tdd.md Seção 2.6/3.1), portanto não é implementado aqui além do caminho literal de
 * RF-25: a quantidade reservada na criação da campanha é espelhada como o próprio estoque
 * promocional dessa campanha, garantindo 0% de divergência nesse fluxo simples.
 */
public class CampanhaPromocionalService {

    private final Map<String, CampanhaPromocional> campanhas = new ConcurrentHashMap<>();
    private final Map<String, Integer> assentosVendidosPorCampanha = new ConcurrentHashMap<>();

    public CampanhaPromocional criar(CampanhaPromocionalRequest request) {
        String campanhaId = UUID.randomUUID().toString();
        CampanhaPromocional campanha = new CampanhaPromocional(
                campanhaId,
                request.rotaId(),
                request.vigenciaInicio(),
                request.vigenciaFim(),
                request.quantidadeAssentosPromocionais(),
                true);
        campanhas.put(campanhaId, campanha);
        assentosVendidosPorCampanha.put(campanhaId, 0);
        return campanha;
    }

    public void venderAssentoPromocional(String campanhaId) {
        assentosVendidosPorCampanha.merge(campanhaId, 1, Integer::sum);
    }

    public boolean contagemPromocionalDivergeDoEstoqueReal(String campanhaId) {
        CampanhaPromocional campanha = campanhas.get(campanhaId);
        if (campanha == null) {
            return true;
        }
        int vendidos = assentosVendidosPorCampanha.getOrDefault(campanhaId, 0);
        return vendidos > campanha.quantidadeAssentosPromocionais();
    }
}
