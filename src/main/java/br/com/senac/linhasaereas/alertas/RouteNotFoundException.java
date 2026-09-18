package br.com.senac.linhasaereas.alertas;

/** Lançada ao tentar criar um alerta de preço para uma rota inexistente no Catálogo — RN-B06. */
public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(String message) {
        super(message);
    }
}
