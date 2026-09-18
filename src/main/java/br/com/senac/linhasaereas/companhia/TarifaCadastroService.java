package br.com.senac.linhasaereas.companhia;

import java.time.Duration;
import java.util.UUID;

/**
 * Cadastro de rotas, horários, classes e tarifas por gestores comerciais das companhias
 * parceiras — RN-F01 (RF-24; RNF-31; RNF-16).
 */
public class TarifaCadastroService {

    private static final Duration LIMITE_PUBLICACAO = Duration.ofMinutes(1);
    private static final Duration LIMITE_USABILIDADE_NAO_TECNICO = Duration.ofMinutes(5);

    public Tarifa cadastrar(NovaTarifaRequest request) {
        validarCamposObrigatorios(request);
        return new Tarifa(
                UUID.randomUUID().toString(),
                request.rotaOrigem(),
                request.rotaDestino(),
                request.horario(),
                request.classe(),
                request.valor(),
                true,
                LIMITE_PUBLICACAO);
    }

    public boolean dentroDoLimiteDeUsabilidadeParaUsuarioNaoTecnico(NovaTarifaRequest request) {
        return request.tempoGastoNoCadastro().compareTo(LIMITE_USABILIDADE_NAO_TECNICO) <= 0;
    }

    public Tarifa editar(String tarifaId, NovaTarifaRequest novaVersao) {
        validarCamposObrigatorios(novaVersao);
        return new Tarifa(
                tarifaId,
                novaVersao.rotaOrigem(),
                novaVersao.rotaDestino(),
                novaVersao.horario(),
                novaVersao.classe(),
                novaVersao.valor(),
                true,
                LIMITE_PUBLICACAO);
    }

    private void validarCamposObrigatorios(NovaTarifaRequest request) {
        if (request.gestorId() == null || request.gestorId().isBlank()) {
            throw new CampoObrigatorioAusenteException("gestorId");
        }
        if (request.rotaOrigem() == null || request.rotaOrigem().isBlank()) {
            throw new CampoObrigatorioAusenteException("rotaOrigem");
        }
        if (request.rotaDestino() == null || request.rotaDestino().isBlank()) {
            throw new CampoObrigatorioAusenteException("rotaDestino");
        }
        if (request.horario() == null) {
            throw new CampoObrigatorioAusenteException("horario");
        }
        if (request.classe() == null || request.classe().isBlank()) {
            throw new CampoObrigatorioAusenteException("classe");
        }
        if (request.valor() == null) {
            throw new CampoObrigatorioAusenteException("valor");
        }
    }
}
