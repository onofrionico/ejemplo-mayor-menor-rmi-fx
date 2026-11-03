package ar.edu.unlu.mayormenor.modelo;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static ar.edu.unlu.mayormenor.modelo.Palo.*;
import static org.junit.jupiter.api.Assertions.*;

class MazoTest {

    @Test
    void mazoMezcladoTiene40CartasDistintas() {
        Mazo mazo = Mazo.mezclado(new Random(1));
        assertEquals(40, mazo.cantidad());
        Set<Carta> vistas = new HashSet<>();
        while (mazo.quedanCartas()) {
            vistas.add(mazo.sacar());
        }
        assertEquals(40, vistas.size());
    }

    @Test
    void saleLaPrimeraDeLaLista() {
        Carta a = new Carta(1, ESPADA);
        Carta b = new Carta(7, ORO);
        Mazo mazo = new Mazo(List.of(a, b));
        assertEquals(a, mazo.sacar());
        assertEquals(1, mazo.cantidad());
    }

    @Test
    void sacarDeUnMazoVacioFalla() {
        Mazo mazo = new Mazo(List.of());
        assertFalse(mazo.quedanCartas());
        assertThrows(IllegalStateException.class, mazo::sacar);
    }
}
