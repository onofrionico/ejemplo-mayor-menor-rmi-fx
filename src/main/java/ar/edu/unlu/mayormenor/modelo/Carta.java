package ar.edu.unlu.mayormenor.modelo;

import java.util.Set;

/**
 * Una carta del mazo español de 40 cartas (sin 8 ni 9).
 * El 10 es la sota, el 11 el caballo y el 12 el rey.
 */
public record Carta(int numero, Palo palo) {

    public static final Set<Integer> NUMEROS_VALIDOS = Set.of(1, 2, 3, 4, 5, 6, 7, 10, 11, 12);

    public Carta {
        if (!NUMEROS_VALIDOS.contains(numero)) {
            throw new IllegalArgumentException("No existe la carta número " + numero + " en el mazo español");
        }
        if (palo == null) {
            throw new IllegalArgumentException("La carta necesita un palo");
        }
    }

    public boolean esMayorQue(Carta otra) {
        return numero > otra.numero;
    }

    public boolean esMenorQue(Carta otra) {
        return numero < otra.numero;
    }

    @Override
    public String toString() {
        return numero + " de " + palo.name().toLowerCase();
    }
}
