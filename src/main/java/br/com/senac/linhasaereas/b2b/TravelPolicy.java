package br.com.senac.linhasaereas.b2b;

import java.util.Set;

/** Política de viagem corporativa de uma empresa cliente (RN-E02). Origem: RF-20; RNF-02; RNF-30. */
public record TravelPolicy(
        String empresaClienteId,
        String classePermitida,
        double tetoValorReais,
        Set<String> companhiasCredenciadas
) {
}
