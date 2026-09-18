package br.com.senac.linhasaereas.companhia;

/**
 * Lançada quando o cadastro de tarifa (RN-F01) está incompleto — ex.: rota sem tarifa.
 */
public class CampoObrigatorioAusenteException extends RuntimeException {

    public CampoObrigatorioAusenteException(String campoFaltante) {
        super("Campo obrigatório ausente no cadastro de tarifa: " + campoFaltante);
    }
}
