package br.com.senac.linhasaereas.cadastro;

/**
 * Lançada quando a cobrança com cartão salvo falha (ex.: cartão expirado) — falha genérica com
 * notificação do erro ao usuário, sem fluxo de reautorização dedicado (RN-C02; decisão do
 * stakeholder em 2026-09-17, plano-tdd.md Seção 3.4, item 10).
 */
public class CobrancaComCartaoSalvoFalhouException extends RuntimeException {
    public CobrancaComCartaoSalvoFalhouException(String message) {
        super(message);
    }
}
