package br.com.senac.linhasaereas.passageiro;

/**
 * Passageiro menor de idade incluído em uma compra — RN-C09 (RF-15; RNF-25).
 * Classe de dados simples, sem regra de negócio.
 */
public record PassageiroMenorDeIdade(String nome, int idade, String responsavelLegalId) {
}
