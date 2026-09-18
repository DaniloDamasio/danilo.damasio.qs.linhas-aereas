package br.com.senac.linhasaereas.seguranca;

/**
 * Exclusão de dados pessoais sob a LGPD, em tensão com a retenção obrigatória de
 * auditoria/fiscal (RNF-25 vs. RNF-23/RNF-28; RN-H02).
 * Assinatura mínima para compilação dos testes; nenhuma regra de negócio implementada ainda.
 */
public class PersonalDataErasureService {

    private static final java.time.Duration PRAZO_MAXIMO = java.time.Duration.ofDays(15);

    public ResultadoExclusaoLgpd solicitarExclusao(TitularDados titular) {
        boolean trilhaFinanceiraExcluida = titular.possuiTransacaoEmRetencaoObrigatoria();
        return new ResultadoExclusaoLgpd(StatusExclusaoLgpd.EXCLUIDO, PRAZO_MAXIMO, trilhaFinanceiraExcluida);
    }
}
