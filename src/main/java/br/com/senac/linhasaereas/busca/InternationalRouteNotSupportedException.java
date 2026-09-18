package br.com.senac.linhasaereas.busca;

/**
 * Lançada quando a busca solicita uma rota internacional — fora de escopo da plataforma, que
 * cobre apenas rotas domésticas (Brasil) (RN-B01/RN-B02; decisão do stakeholder em 2026-09-17,
 * plano-tdd.md Seção 3.4, item 8).
 */
public class InternationalRouteNotSupportedException extends RuntimeException {
    public InternationalRouteNotSupportedException(String message) {
        super(message);
    }
}
