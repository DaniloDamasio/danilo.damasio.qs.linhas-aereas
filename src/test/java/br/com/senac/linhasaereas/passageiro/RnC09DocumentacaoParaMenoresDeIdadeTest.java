package br.com.senac.linhasaereas.passageiro;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * RN-C09 — Documentação exigida para menores de idade.
 * Origem: RF-15; RNF-25.
 *
 * Nota: "Documentos concretos exigidos por idade/rota" é um Gap registrado no plano-tdd.md
 * (Seção 2.3, RN-C09; lacuna L-04) — não é um caso de teste, apenas uma lacuna a registrar.
 */
class RnC09DocumentacaoParaMenoresDeIdadeTest {

    @Test
    void rnC09_cf_passageiroMenorDeIdadeIncluidoNaCompraExibeRegrasDeDocumentacao() {
        // Arrange
        DocumentacaoMenorService service = new DocumentacaoMenorService();
        PassageiroMenorDeIdade crianca = new PassageiroMenorDeIdade("Crianca Teste", 5, "responsavel-1");

        // Act
        var regras = service.regrasDeDocumentacaoAplicaveis(crianca);

        // Assert: regras de documentação devem ser exibidas durante o fluxo
        assertFalse(regras.isEmpty(), "regras de documentação devem ser exibidas para passageiro menor de idade");
    }

    @Test
    @Disabled("Lacuna: o mecanismo de consentimento do responsável para coleta de dados de menores (LGPD, "
            + "RNF-25) não é detalhado pela fonte (plano-tdd.md, Seção 2.3 RN-C09). "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnC09_conf_dadosDeMenorColetadosSemConsentimentoApropriado_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
