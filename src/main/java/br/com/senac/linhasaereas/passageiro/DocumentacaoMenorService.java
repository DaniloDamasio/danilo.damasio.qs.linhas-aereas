package br.com.senac.linhasaereas.passageiro;

import java.util.List;

/**
 * Documentação exigida para menores de idade — RN-C09 (RF-15; RNF-25).
 * Documentos concretos exigidos por idade/rota dependem de regra de cada companhia/ANAC — não
 * especificados pela fonte (lacuna L-04); aqui apenas a regra geral aplicável.
 */
public class DocumentacaoMenorService {

    public List<String> regrasDeDocumentacaoAplicaveis(PassageiroMenorDeIdade menor) {
        return List.of(
                "Documento de identificação do menor (certidão de nascimento ou RG)",
                "Autorização/acompanhamento do responsável legal: " + menor.responsavelLegalId());
    }

    public void registrarConsentimentoResponsavel(PassageiroMenorDeIdade menor, boolean consentimentoObtido) {
        if (!consentimentoObtido) {
            throw new ConsentimentoNaoObtidoException(
                    "coleta de dados do menor " + menor.nome() + " requer consentimento do responsável (LGPD, RNF-25)");
        }
    }
}
