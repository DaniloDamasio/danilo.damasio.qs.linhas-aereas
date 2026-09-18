package br.com.senac.linhasaereas.b2b;

/**
 * Requisição à API pública B2B (RN-E04).
 *
 * @param caminho recurso solicitado
 * @param versaoApi versão de API usada pelo cliente (ex.: "v1", "v2")
 * @param mesesDesdeAPublicacaoDaProximaVersao meses transcorridos desde que uma versão mais nova
 *        foi publicada; usado para avaliar a janela de retrocompatibilidade de 12 meses (RNF-33)
 */
public record ApiRequest(String caminho, String versaoApi, int mesesDesdeAPublicacaoDaProximaVersao) {
}
