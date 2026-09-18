package br.com.senac.linhasaereas.passageiro;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-C06 — Compra simultânea de múltiplos passageiros com preenchimento assistido.
 * Origem: RF-12; RNF-17.
 *
 * Nota: "Número máximo de passageiros por transação" é um Gap registrado no plano-tdd.md
 * (Seção 2.3, RN-C06) — não é um caso de teste, apenas uma lacuna a registrar.
 */
class RnC06CompraMultiplosPassageirosTest {

    @Test
    void rnC06_cf_familiaDeQuatroPassageirosEmUmaTransacao() {
        // Arrange
        CompraMultiplosPassageirosService service = new CompraMultiplosPassageirosService();
        List<DadosPassageiro> familia = List.of(
                new DadosPassageiro("Adulto 1", "111.111.111-11", 35),
                new DadosPassageiro("Adulto 2", "222.222.222-22", 34),
                new DadosPassageiro("Crianca 1", "333.333.333-33", 8),
                new DadosPassageiro("Crianca 2", "444.444.444-44", 5)
        );

        // Act
        ResultadoCompraMultipla resultado = service.comprar(familia);

        // Assert: todos incluídos em uma única transação
        assertEquals(4, resultado.totalPassageiros());
    }

    @Test
    void rnC06_cf_reusoDeDadosDoPrimeiroParaOsDemaisPassageirosReduzCamposManuais() {
        // Arrange
        CompraMultiplosPassageirosService service = new CompraMultiplosPassageirosService();
        List<DadosPassageiro> passageiros = List.of(
                new DadosPassageiro("Titular", "111.111.111-11", 40),
                new DadosPassageiro("Acompanhante", "222.222.222-22", 38)
        );

        // Act
        ResultadoCompraMultipla resultado = service.comprar(passageiros);

        // Assert: redução de ao menos 70% dos campos preenchidos manualmente do 2º ao Nº passageiro
        assertTrue(resultado.percentualCamposReaproveitados() >= 70.0,
                "reuso de dados do 1º passageiro deve atingir ao menos 70%");
    }

    @Test
    void rnC06_lim_reusoExatamenteEm70PorCentoAtingeOPisoMinimo() {
        // Arrange
        CompraMultiplosPassageirosService service = new CompraMultiplosPassageirosService();
        List<DadosPassageiro> passageiros = List.of(
                new DadosPassageiro("Titular", "111.111.111-11", 40),
                new DadosPassageiro("Acompanhante", "222.222.222-22", 38)
        );

        // Act
        ResultadoCompraMultipla resultado = service.comprar(passageiros);

        // Assert: exatamente no piso mínimo ainda é conforme
        assertEquals(70.0, resultado.percentualCamposReaproveitados());
    }

    @Test
    void rnC06_inv_transacaoComZeroPassageirosERejeitada() {
        // Arrange
        CompraMultiplosPassageirosService service = new CompraMultiplosPassageirosService();
        List<DadosPassageiro> nenhumPassageiro = List.of();

        // Act & Assert
        assertThrows(NenhumPassageiroSelecionadoException.class, () -> service.comprar(nenhumPassageiro));
    }
}
