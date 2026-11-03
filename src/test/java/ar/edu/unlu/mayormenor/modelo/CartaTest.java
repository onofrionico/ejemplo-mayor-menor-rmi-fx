package ar.edu.unlu.mayormenor.modelo;

import org.junit.jupiter.api.Test;

import static ar.edu.unlu.mayormenor.modelo.Palo.*;
import static org.junit.jupiter.api.Assertions.*;

class CartaTest {

    @Test
    void noExistenEl8NiEl13() {
        assertThrows(IllegalArgumentException.class, () -> new Carta(8, ORO));
        assertThrows(IllegalArgumentException.class, () -> new Carta(13, ORO));
    }

    @Test
    void comparaPorNumero() {
        assertTrue(new Carta(12, COPA).esMayorQue(new Carta(1, ORO)));
        assertTrue(new Carta(1, ORO).esMenorQue(new Carta(12, COPA)));
    }

    @Test
    void mismoNumeroNoEsMayorNiMenor() {
        assertFalse(new Carta(5, ORO).esMayorQue(new Carta(5, BASTO)));
        assertFalse(new Carta(5, ORO).esMenorQue(new Carta(5, BASTO)));
    }
}
