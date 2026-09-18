package br.com.senac.linhasaereas.usabilidade;

import java.time.Duration;

/** Resultado de uma renderização em um dado viewport (RNF-18, RN-J01). */
public record RenderResult(boolean funcional, Duration largestContentfulPaint) {
}
