package br.com.senac.linhasaereas.passageiro;

/** Lançada quando o checkout é iniciado sem nenhum passageiro selecionado (RN-C06). */
public class NenhumPassageiroSelecionadoException extends RuntimeException {
    public NenhumPassageiroSelecionadoException(String message) {
        super(message);
    }
}
