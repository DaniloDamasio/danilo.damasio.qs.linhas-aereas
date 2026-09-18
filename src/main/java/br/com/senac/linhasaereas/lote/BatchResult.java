package br.com.senac.linhasaereas.lote;

import java.util.List;

/** Resultado agregado de uma emissão em lote (RN-A06). */
public record BatchResult(List<BatchItemResult> itens) {
}
