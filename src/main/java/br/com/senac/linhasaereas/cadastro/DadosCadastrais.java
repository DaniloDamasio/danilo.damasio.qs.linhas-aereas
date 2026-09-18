package br.com.senac.linhasaereas.cadastro;

/**
 * Dados cadastrais recorrentes do usuário — RN-C02 (RF-08).
 * Classe de dados simples, sem regra de negócio.
 */
public record DadosCadastrais(
        String usuarioId,
        String cpf,
        String endereco,
        CartaoSalvo cartaoSalvo,
        String numeroFidelidade
) {
}
