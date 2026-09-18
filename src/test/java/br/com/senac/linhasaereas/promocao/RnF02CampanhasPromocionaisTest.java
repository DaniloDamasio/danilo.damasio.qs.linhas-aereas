package br.com.senac.linhasaereas.promocao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-F02 — Campanhas promocionais com quantidade de assentos promocionais.
 * Origem: RF-25; ADR-06 [proposta, não decisão].
 */
class RnF02CampanhasPromocionaisTest {

    @Test
    void rnF02_cf_criacaoDeCampanhaComRotaVigenciaEQuantidadeDeAssentosValidas() {
        // Arrange
        CampanhaPromocionalService service = new CampanhaPromocionalService();
        CampanhaPromocionalRequest request = new CampanhaPromocionalRequest(
                "rota-BH-SP", LocalDate.now(), LocalDate.now().plusDays(30), 10);

        // Act
        CampanhaPromocional campanha = service.criar(request);

        // Assert: campanha criada e quantidade refletida na disponibilidade real
        assertTrue(campanha.ativa());
        assertEquals(10, campanha.quantidadeAssentosPromocionais());
        assertFalse(service.contagemPromocionalDivergeDoEstoqueReal(campanha.campanhaId()),
                "contagem promocional não pode divergir do estoque real (RESTRIÇÃO-CRÍTICA-01)");
    }

    @Test
    @Disabled("Comportamento para campanha criada com quantidade de assentos promocionais igual a zero "
            + "(rejeitar vs. tratar como campanha inativa) não é definido explicitamente pela fonte — "
            + "lacuna menor registrada no plano-tdd.md, Seção 2.6, RN-F02, caso LIM. "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnF02_lim_quantidadeDeAssentosPromocionaisIgualAZero_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    @Disabled("Ponto exato de bloqueio quando a demanda por assentos promocionais excede o lote cadastrado "
            + "depende de ADR-06 (proposta, não decisão) sobre onde a contagem promocional é fonte de "
            + "verdade — Estoque ou Catálogo (DIV-02, plano-tdd.md Seção 3.1). "
            + "Não escrever asserção definitiva até adoção formal do ADR.")
    void rnF02_conf_assentosPromocionaisVendidosExcedemQuantidadeCadastrada_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }

    @Test
    void rnF02_proib_vendaForaDaCampanhaContabilizadaComoPromocionalOuViceVersaEProibida() {
        // Arrange
        CampanhaPromocionalService service = new CampanhaPromocionalService();
        CampanhaPromocionalRequest request = new CampanhaPromocionalRequest(
                "rota-BH-SP", LocalDate.now(), LocalDate.now().plusDays(30), 5);
        CampanhaPromocional campanha = service.criar(request);

        // Act
        boolean diverge = service.contagemPromocionalDivergeDoEstoqueReal(campanha.campanhaId());

        // Assert: proibido por decorrência direta de RESTRIÇÃO-CRÍTICA-01
        assertFalse(diverge, "divergência entre contagem de campanha e estoque real é proibida");
    }
}
