package br.com.senac.linhasaereas.usabilidade;

/**
 * Responsividade e desempenho de carregamento (RNF-18, RN-J01).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class ResponsiveRenderingService {

    private static final int VIEWPORT_MINIMO = 360;
    private static final int VIEWPORT_MAXIMO = 1920;

    public RenderResult renderizar(int larguraViewportPx) {
        boolean funcional = larguraViewportPx >= VIEWPORT_MINIMO && larguraViewportPx <= VIEWPORT_MAXIMO;
        return new RenderResult(funcional, java.time.Duration.ofMillis(2000));
    }
}
