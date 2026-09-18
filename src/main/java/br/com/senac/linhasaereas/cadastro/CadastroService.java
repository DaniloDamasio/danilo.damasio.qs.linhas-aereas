package br.com.senac.linhasaereas.cadastro;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Retenção/pré-preenchimento de dados cadastrais recorrentes — RN-C02 (RF-08).
 */
public class CadastroService {

    public DadosCadastrais preencherAutomaticamente(String usuarioId) {
        return new DadosCadastrais(
                usuarioId,
                "000.000.000-00",
                "Endereço cadastrado do usuário " + usuarioId,
                new CartaoSalvo("token-" + usuarioId, LocalDate.now().plusYears(2)),
                "FID-" + usuarioId);
    }

    public void cobrarComCartaoSalvo(CartaoSalvo cartao, BigDecimal valor) {
        // Decisão do stakeholder (2026-09-17, plano-tdd.md Seção 3.4, item 10): cartão salvo expirado
        // falha de forma genérica, com notificação ao usuário, sem fluxo de reautorização dedicado.
        if (cartao.expirado(LocalDate.now())) {
            throw new CobrancaComCartaoSalvoFalhouException(
                    "não foi possível cobrar o cartão salvo — falha genérica, notifique o usuário do erro");
        }
    }
}
