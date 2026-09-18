package br.com.senac.linhasaereas.seguranca;

/** Token que substitui o PAN (Primary Account Number) de um cartão — RNF-24 (RN-H01). */
public record CardToken(String token, String ultimosQuatroDigitos) {
}
