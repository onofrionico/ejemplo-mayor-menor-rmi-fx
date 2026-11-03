package ar.edu.unlu.mayormenor.modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** Pila de cartas de la que se saca siempre la de arriba. */
public class Mazo {

    private final Deque<Carta> cartas;

    /** Crea un mazo con las cartas en el orden dado: la primera de la lista es la primera que sale. */
    public Mazo(List<Carta> enOrden) {
        this.cartas = new ArrayDeque<>(enOrden);
    }

    /** Las 40 cartas del mazo español, mezcladas. */
    public static Mazo mezclado(Random random) {
        List<Carta> todas = new ArrayList<>();
        for (Palo palo : Palo.values()) {
            for (int numero : Carta.NUMEROS_VALIDOS) {
                todas.add(new Carta(numero, palo));
            }
        }
        Collections.shuffle(todas, random);
        return new Mazo(todas);
    }

    public Carta sacar() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("El mazo está vacío");
        }
        return cartas.pop();
    }

    public boolean quedanCartas() {
        return !cartas.isEmpty();
    }

    public int cantidad() {
        return cartas.size();
    }
}
