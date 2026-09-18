package br.com.senac.linhasaereas.passageiro;

/** Lançada quando dados de um menor são coletados sem consentimento apropriado do responsável (RN-C09; RNF-25). */
public class ConsentimentoNaoObtidoException extends RuntimeException {
    public ConsentimentoNaoObtidoException(String message) {
        super(message);
    }
}
