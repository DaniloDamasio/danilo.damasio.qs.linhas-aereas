package br.com.senac.linhasaereas.usabilidade;

/**
 * Compatibilidade de plataforma (SO/navegadores) — RNF-32, RNF-33, RN-J02.
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PlatformCompatibilityService {

    private static final java.util.Set<String> PLATAFORMAS_SUPORTADAS =
            java.util.Set.of("iOS", "Android", "Chrome", "Safari", "Edge");

    public SuporteResultado verificarSuporte(String plataforma, String versao) {
        if (PLATAFORMAS_SUPORTADAS.contains(plataforma)) {
            return SuporteResultado.COMPLETO;
        }
        return SuporteResultado.NAO_GARANTIDO;
    }
}
