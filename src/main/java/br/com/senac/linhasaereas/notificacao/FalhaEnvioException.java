package br.com.senac.linhasaereas.notificacao;

/** Lançada quando um canal de notificação falha ao entregar a confirmação (RN-C04). */
public class FalhaEnvioException extends RuntimeException {
    public FalhaEnvioException(String message) {
        super(message);
    }
}
