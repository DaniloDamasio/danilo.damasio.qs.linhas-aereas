package br.com.senac.linhasaereas.continuidade;

/**
 * Lançada quando a perda de dados de estoque observada excede o RPO definido — RN-F03.
 */
public class PerdaDeDadosAlemDoRpoException extends RuntimeException {

    public PerdaDeDadosAlemDoRpoException(String mensagem) {
        super(mensagem);
    }
}
