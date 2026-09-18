package br.com.senac.linhasaereas.passageiro;

import java.util.List;

/**
 * Documentação exigida para menores de idade — RN-C09 (RF-15; RNF-25).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class DocumentacaoMenorService {

    public List<String> regrasDeDocumentacaoAplicaveis(PassageiroMenorDeIdade menor) {
        throw new UnsupportedOperationException("exibição de regras de documentação ainda não implementada (RN-C09)");
    }

    public void registrarConsentimentoResponsavel(PassageiroMenorDeIdade menor, boolean consentimentoObtido) {
        throw new UnsupportedOperationException("registro de consentimento do responsável ainda não implementado (RN-C09/RNF-25)");
    }
}
