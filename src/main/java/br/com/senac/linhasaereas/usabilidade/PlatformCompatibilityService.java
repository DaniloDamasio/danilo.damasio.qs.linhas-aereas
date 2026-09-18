package br.com.senac.linhasaereas.usabilidade;

/**
 * Compatibilidade de plataforma (SO/navegadores) — RNF-32, RNF-33, RN-J02.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PlatformCompatibilityService {

    public SuporteResultado verificarSuporte(String plataforma, String versao) {
        throw new UnsupportedOperationException("verificação de compatibilidade de plataforma ainda não implementada (RN-J02)");
    }
}
