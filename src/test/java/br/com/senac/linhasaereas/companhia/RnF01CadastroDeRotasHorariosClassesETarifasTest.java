package br.com.senac.linhasaereas.companhia;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RN-F01 — Cadastro de rotas, horários, classes e tarifas.
 * Origem: RF-24; RNF-31; RNF-16.
 */
class RnF01CadastroDeRotasHorariosClassesETarifasTest {

    private NovaTarifaRequest requestCompleto(Duration tempoGasto) {
        return new NovaTarifaRequest(
                "gestor-1", "BH", "SP", LocalTime.of(8, 0), "ECONOMICA",
                new BigDecimal("450.00"), tempoGasto);
    }

    @Test
    void rnF01_cf_gestorComercialCadastraNovaTarifaComDadosCompletosEValidos() {
        // Arrange
        TarifaCadastroService service = new TarifaCadastroService();
        NovaTarifaRequest request = requestCompleto(Duration.ofMinutes(2));

        // Act
        Tarifa tarifa = service.cadastrar(request);

        // Assert: publicação em vigor em até 1 minuto, sem intervenção técnica
        assertTrue(tarifa.publicada(), "tarifa deve ser publicada automaticamente após o cadastro");
        assertTrue(tarifa.tempoAtePublicacao().compareTo(Duration.ofMinutes(1)) <= 0,
                "publicação deve ocorrer em até 1 minuto");
    }

    @Test
    void rnF01_lim_cadastroConcluidoEmExatamenteCincoMinutosPorUsuarioNaoTecnico() {
        // Arrange: baseline sugerida de usabilidade para conclusão do fluxo sem apoio técnico
        TarifaCadastroService service = new TarifaCadastroService();
        NovaTarifaRequest request = requestCompleto(Duration.ofMinutes(5));

        // Act
        boolean dentroDoLimite = service.dentroDoLimiteDeUsabilidadeParaUsuarioNaoTecnico(request);

        // Assert: exatamente no limite ainda é conforme
        assertTrue(dentroDoLimite, "cadastro em exatamente 5 minutos deve ser considerado conforme");
    }

    @Test
    void rnF01_inv_cadastroComCampoObrigatorioAusenteERejeitadoComIndicacaoDoCampo() {
        // Arrange: rota sem tarifa (valor ausente)
        TarifaCadastroService service = new TarifaCadastroService();
        NovaTarifaRequest requestIncompleto = new NovaTarifaRequest(
                "gestor-1", "BH", "SP", LocalTime.of(8, 0), "ECONOMICA", null, Duration.ofMinutes(2));

        // Act & Assert
        assertThrows(CampoObrigatorioAusenteException.class, () -> service.cadastrar(requestIncompleto));
    }

    @Test
    @Disabled("Comportamento de concorrência para duas alterações de tarifa da mesma rota quase simultâneas "
            + "pelo mesmo gestor (last-write-wins vs. bloqueio otimista) não é definido pela fonte — "
            + "lacuna registrada no plano-tdd.md, Seção 2.6, RN-F01, caso CONF. "
            + "Não escrever asserção definitiva até essa decisão.")
    void rnF01_conf_duasAlteracoesDeTarifaDaMesmaRotaQuaseSimultaneasPeloMesmoGestor_pendenteDeDefinicaoDeFonte() {
        // Intencionalmente não implementado: ver anotação @Disabled acima.
    }
}
