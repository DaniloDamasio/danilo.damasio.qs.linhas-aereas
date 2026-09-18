package br.com.senac.linhasaereas.companhia;

/**
 * Cadastro de rotas, horários, classes e tarifas por gestores comerciais das companhias
 * parceiras — RN-F01 (RF-24; RNF-31; RNF-16).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class TarifaCadastroService {

    public Tarifa cadastrar(NovaTarifaRequest request) {
        throw new UnsupportedOperationException("cadastro de tarifa ainda não implementado (RN-F01)");
    }

    public boolean dentroDoLimiteDeUsabilidadeParaUsuarioNaoTecnico(NovaTarifaRequest request) {
        throw new UnsupportedOperationException(
                "verificação do limite de usabilidade do cadastro ainda não implementada (RN-F01)");
    }

    public Tarifa editar(String tarifaId, NovaTarifaRequest novaVersao) {
        throw new UnsupportedOperationException("edição concorrente de tarifa ainda não implementada (RN-F01)");
    }
}
