package br.com.senac.linhasaereas.cadastro;

import java.time.LocalDate;

/**
 * Cartão de pagamento salvo do usuário — RN-C02 (RF-08).
 * Classe de dados simples, sem regra de negócio.
 */
public record CartaoSalvo(String tokenCartao, LocalDate validade) {

    public boolean expirado(LocalDate referencia) {
        return validade.isBefore(referencia);
    }
}
