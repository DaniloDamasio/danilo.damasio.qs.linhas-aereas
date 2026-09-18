package br.com.senac.linhasaereas.fiscal;

/** Lançada quando o integrador fiscal externo está indisponível para emissão (RN-C05). */
public class EmissaoFiscalIndisponivelException extends RuntimeException {
    public EmissaoFiscalIndisponivelException(String message) {
        super(message);
    }
}
