package br.com.senac.linhasaereas.alertas;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-B06 — Alerta de preço por rota/data.
 * Origem: RF-06. Sem componente arquitetural definido — gap identificado em revisao-painel-atam.md §6.
 *
 * Nota: a linha "Canal, frequência de verificação e limiar de 'queda de preço'" da tabela do
 * plano é um Gap, não um caso de teste — não há método de teste correspondente a ela.
 */
class RnB06AlertaDePrecoPorRotaDataTest {

    private static final String ROTA = "BH-SP";

    @Test
    void rnB06_cf_configurarAlertaParaRotaEData() {
        // Arrange
        PriceAlertService service = new PriceAlertService();

        // Act
        String alertaId = service.criarAlerta(ROTA, LocalDate.now().plusDays(10), new BigDecimal("50"));

        // Assert: alerta registrado
        assertFalse(alertaId == null || alertaId.isBlank(), "criação de alerta deve retornar um identificador válido");
    }

    @Test
    void rnB06_cf_precoCaiAbaixoDoConfiguradoUsuarioENotificado() {
        // Arrange
        PriceAlertService service = new PriceAlertService();
        String alertaId = service.criarAlerta(ROTA, LocalDate.now().plusDays(10), new BigDecimal("50"));

        // Act
        boolean notificado = service.precoCaiuAbaixoDoConfigurado(alertaId, new BigDecimal("40"));

        // Assert: usuário notificado quando o preço cai abaixo do configurado
        assertTrue(notificado);
    }

    @Test
    void rnB06_inv_alertaParaRotaSemVoosCadastrados() {
        // Arrange
        PriceAlertService service = new PriceAlertService();

        // Act & Assert: rota inexistente no Catálogo deve ser rejeitada na criação do alerta
        assertThrows(RouteNotFoundException.class,
                () -> service.criarAlerta("ROTA-INEXISTENTE", LocalDate.now().plusDays(10), new BigDecimal("50")));
    }
}
