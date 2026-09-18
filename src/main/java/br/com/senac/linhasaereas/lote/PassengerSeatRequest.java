package br.com.senac.linhasaereas.lote;

/** Item de uma planilha de emissão em lote — arquitetura.md §11.3/§12.6 (RN-A06). */
public record PassengerSeatRequest(String passageiroId, String vooId, String assentoId) {
}
