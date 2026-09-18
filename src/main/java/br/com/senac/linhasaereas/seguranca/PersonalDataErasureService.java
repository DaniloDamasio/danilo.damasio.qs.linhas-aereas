package br.com.senac.linhasaereas.seguranca;

/**
 * Exclusão de dados pessoais sob a LGPD, em tensão com a retenção obrigatória de
 * auditoria/fiscal (RNF-25 vs. RNF-23/RNF-28; RN-H02).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PersonalDataErasureService {

    public ResultadoExclusaoLgpd solicitarExclusao(TitularDados titular) {
        throw new UnsupportedOperationException("exclusão de dados pessoais (LGPD) ainda não implementada (RN-H02)");
    }
}
