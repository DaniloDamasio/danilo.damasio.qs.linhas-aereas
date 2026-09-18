package br.com.senac.linhasaereas.b2b;

/** Lançada quando o arquivo de planilha enviado está em formato não suportado (RN-E03). */
public class UnsupportedSpreadsheetFormatException extends RuntimeException {
    public UnsupportedSpreadsheetFormatException(String message) {
        super(message);
    }
}
