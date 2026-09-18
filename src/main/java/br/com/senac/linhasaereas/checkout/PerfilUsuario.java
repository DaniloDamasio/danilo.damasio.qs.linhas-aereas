package br.com.senac.linhasaereas.checkout;

/**
 * Perfil de dados salvos do usuário para checkout rápido — RN-C01 (RF-07; RNF-05; RNF-13).
 * Classe de dados simples, sem regra de negócio.
 */
public record PerfilUsuario(String usuarioId, boolean dadosCompletos, boolean cartaoSalvo) {
}
