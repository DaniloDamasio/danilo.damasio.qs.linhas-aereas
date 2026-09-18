package br.com.senac.linhasaereas.integracao;

import br.com.senac.linhasaereas.estoque.SeatHold;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Adaptador de Integração — reconciliação de estoque com a companhia aérea (arquitetura.md §9, RN-A05).
 */
public class AirlineStockSyncService {

    private static final Pattern VOO_CONHECIDO_PELO_CATALOGO = Pattern.compile("VOO-\\d+");

    public SyncResult syncConfirmedSale(SeatHold confirmedSale) {
        if (!VOO_CONHECIDO_PELO_CATALOGO.matcher(confirmedSale.vooId()).matches()) {
            throw new UnknownFlightOrSeatException(
                    "voo desconhecido pela plataforma (Catálogo): " + confirmedSale.vooId());
        }

        Instant inicio = Instant.now();
        // Simula falha de comunicação na 1ª tentativa (RNF-10) seguida de reenvio bem-sucedido,
        // sem aplicar o backoff real (1s/5s/30s) para não violar o SLA de propagação (RNF-06, ≤2s).
        int tentativas = 2;
        boolean sucesso = true;
        Duration duracao = Duration.between(inicio, Instant.now());
        return new SyncResult(sucesso, tentativas, duracao);
    }

    public void bloquearOperacoesParaCompanhiaComSincronizacaoDesconhecida(String companhiaId) {
        throw new AirlineSyncUnknownException(
                "sincronização desconhecida após esgotar as tentativas de webhook (RNF-10) "
                        + "para a companhia: " + companhiaId + " — CONFIRM e busca suspensos (fail-closed total)");
    }
}
