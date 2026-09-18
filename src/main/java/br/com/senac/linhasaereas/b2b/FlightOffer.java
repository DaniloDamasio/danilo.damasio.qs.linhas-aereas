package br.com.senac.linhasaereas.b2b;

/** Oferta de voo considerada pelo motor de políticas de viagem (RN-E02). */
public record FlightOffer(
        String vooId,
        String companhiaAerea,
        String classe,
        double preco
) {
}
