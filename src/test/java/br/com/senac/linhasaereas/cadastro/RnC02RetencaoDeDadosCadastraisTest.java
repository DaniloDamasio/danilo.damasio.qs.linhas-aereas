package br.com.senac.linhasaereas.cadastro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RN-C02 — Retenção/pré-preenchimento de dados cadastrais recorrentes.
 * Origem: RF-08.
 */
class RnC02RetencaoDeDadosCadastraisTest {

    @Test
    void rnC02_cf_segundaCompraDoMesmoUsuarioPreencheCamposAutomaticamente() {
        // Arrange
        CadastroService service = new CadastroService();

        // Act
        DadosCadastrais dados = service.preencherAutomaticamente("usuario-1");

        // Assert
        assertNotNull(dados.cpf(), "CPF deve vir pré-preenchido");
        assertNotNull(dados.endereco(), "endereço deve vir pré-preenchido");
        assertNotNull(dados.cartaoSalvo(), "forma de pagamento salva deve vir pré-preenchida");
        assertNotNull(dados.numeroFidelidade(), "dados de fidelidade devem vir pré-preenchidos");
    }

    @Test
    void rnC02_conf_cartaoSalvoExpiradoFalhaDeFormaGenericaComNotificacaoAoUsuarioSemFluxoDeReautorizacao() {
        // Arrange
        CadastroService service = new CadastroService();
        CartaoSalvo cartaoVencido = new CartaoSalvo("token-abc", LocalDate.of(2020, 1, 1));

        // Act & Assert: Decisão confirmada pelo stakeholder em 2026-09-17 (plano-tdd.md Seção 3.4, item 10) —
        // não há fluxo de reautorização dedicado; a cobrança com cartão salvo expirado falha de forma
        // genérica, com notificação do erro ao usuário (não falha silenciosamente). A exigência de
        // reautorização explícita anteriormente registrada como caso PROIB desta RN foi removida.
        assertThrows(CobrancaComCartaoSalvoFalhouException.class,
                () -> service.cobrarComCartaoSalvo(cartaoVencido, new BigDecimal("500.00")));
    }

    @Test
    void rnC02_cf_dadosPreenchidosCorrespondemAoUsuarioSolicitado() {
        // Arrange
        CadastroService service = new CadastroService();

        // Act
        DadosCadastrais dados = service.preencherAutomaticamente("usuario-2");

        // Assert
        assertEquals("usuario-2", dados.usuarioId());
    }
}
