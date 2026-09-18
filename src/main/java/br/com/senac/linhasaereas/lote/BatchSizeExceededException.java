package br.com.senac.linhasaereas.lote;

/**
 * Lançada quando um lote de emissão excede o limite de 50 passageiros — rejeitado inteiramente na
 * ingestão, sem processamento parcial (RN-A06; decisão do stakeholder em 2026-09-17, plano-tdd.md
 * Seção 3.4, item 9).
 */
public class BatchSizeExceededException extends RuntimeException {
    public BatchSizeExceededException(String message) {
        super(message);
    }
}
