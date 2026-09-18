package br.com.senac.linhasaereas.cadastro;

import java.math.BigDecimal;

/**
 * Retenção/pré-preenchimento de dados cadastrais recorrentes — RN-C02 (RF-08).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class CadastroService {

    public DadosCadastrais preencherAutomaticamente(String usuarioId) {
        throw new UnsupportedOperationException("pré-preenchimento cadastral ainda não implementado (RN-C02)");
    }

    public void cobrarComCartaoSalvo(CartaoSalvo cartao, BigDecimal valor) {
        throw new UnsupportedOperationException(
                "cobrança com cartão salvo ainda não implementada; cartão expirado deve falhar de forma "
                        + "genérica, com notificação ao usuário, sem fluxo de reautorização dedicado (RN-C02)");
    }
}
