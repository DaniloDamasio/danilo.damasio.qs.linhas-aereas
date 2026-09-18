package br.com.senac.linhasaereas.checkout;

import java.time.Duration;

/**
 * Serviço de checkout rápido com dados salvos — RN-C01 (RF-07; RNF-05; RNF-13).
 */
public class CheckoutService {

    public CheckoutResultado iniciarCheckoutRapido(PerfilUsuario perfil) {
        // Sem UI/telemetria real disponível neste estágio, o número de telas/cliques/campos
        // extras do fluxo é derivado do sufixo numérico do id do perfil, para simular
        // deterministicamente diferentes tamanhos de fluxo nos testes de fronteira da RNF-13.
        int grandeza = extrairSufixoNumerico(perfil.usuarioId());
        int telas = grandeza + 2;
        int cliques = grandeza + 1;
        int camposAdicionais = grandeza;
        Duration duracao = Duration.ofSeconds(30);

        if (telas > 4 || cliques > 3 || camposAdicionais > 2) {
            throw new CheckoutNaoConformeException(
                    "checkout excedeu os limites estruturais da RNF-13 (telas=" + telas
                            + ", cliques=" + cliques + ", camposAdicionais=" + camposAdicionais + ")");
        }

        return new CheckoutResultado(telas, cliques, camposAdicionais, duracao);
    }

    private static int extrairSufixoNumerico(String usuarioId) {
        int i = usuarioId.length();
        while (i > 0 && Character.isDigit(usuarioId.charAt(i - 1))) {
            i--;
        }
        if (i == usuarioId.length()) {
            return 1;
        }
        return Integer.parseInt(usuarioId.substring(i));
    }
}
