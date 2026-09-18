package br.com.senac.linhasaereas.alertas;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Alerta de preço por rota/data — RF-06 (RN-B06).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PriceAlertService {

    public String criarAlerta(String rota, LocalDate data, BigDecimal limiteQueda) {
        throw new UnsupportedOperationException("criação de alerta de preço ainda não implementada (RN-B06)");
    }

    public boolean precoCaiuAbaixoDoConfigurado(String alertaId, BigDecimal precoAtual) {
        throw new UnsupportedOperationException("avaliação de queda de preço ainda não implementada (RN-B06)");
    }
}
