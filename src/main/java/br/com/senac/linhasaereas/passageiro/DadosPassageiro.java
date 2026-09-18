package br.com.senac.linhasaereas.passageiro;

/**
 * Dados de um passageiro em uma compra com múltiplos passageiros — RN-C06 (RF-12; RNF-17).
 * Classe de dados simples, sem regra de negócio.
 */
public record DadosPassageiro(String nome, String documento, int idade) {
}
