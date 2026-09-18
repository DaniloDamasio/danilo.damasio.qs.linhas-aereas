package br.com.senac.linhasaereas.seguranca;

/** Titular de dados pessoais que pode solicitar exclusão sob a LGPD (RNF-25, RN-H02). */
public record TitularDados(String id, boolean menorDeIdade, boolean possuiTransacaoEmRetencaoObrigatoria) {
}
