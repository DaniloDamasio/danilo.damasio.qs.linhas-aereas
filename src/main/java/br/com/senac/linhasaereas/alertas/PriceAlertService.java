package br.com.senac.linhasaereas.alertas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Alerta de preço por rota/data — RF-06 (RN-B06).
 */
public class PriceAlertService {

    private static final Set<String> ROTAS_CADASTRADAS = Set.of("BH-SP", "SP-BH", "RJ-BH", "BH-RJ");

    private final Map<String, BigDecimal> limiteQuedaPorAlerta = new ConcurrentHashMap<>();

    public String criarAlerta(String rota, LocalDate data, BigDecimal limiteQueda) {
        if (rota == null || !ROTAS_CADASTRADAS.contains(rota)) {
            throw new RouteNotFoundException("rota não encontrada no Catálogo: " + rota);
        }
        String alertaId = UUID.randomUUID().toString();
        limiteQuedaPorAlerta.put(alertaId, limiteQueda);
        return alertaId;
    }

    public boolean precoCaiuAbaixoDoConfigurado(String alertaId, BigDecimal precoAtual) {
        BigDecimal limiteQueda = limiteQuedaPorAlerta.get(alertaId);
        if (limiteQueda == null) {
            throw new RouteNotFoundException("alerta não encontrado: " + alertaId);
        }
        return precoAtual.compareTo(limiteQueda) < 0;
    }
}
