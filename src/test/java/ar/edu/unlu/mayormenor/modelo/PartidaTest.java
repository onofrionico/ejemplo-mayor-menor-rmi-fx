package ar.edu.unlu.mayormenor.modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static ar.edu.unlu.mayormenor.modelo.Apuesta.*;
import static ar.edu.unlu.mayormenor.modelo.Palo.*;
import static org.junit.jupiter.api.Assertions.*;

class PartidaTest {

    /** Observador espía: anota los eventos que recibe. */
    static class ObservadorEspia implements IObservadorRemoto {
        final List<Object> eventos = new ArrayList<>();

        @Override
        public void actualizar(IObservableRemoto observable, Object evento) {
            eventos.add(evento);
        }

        Object ultimo() {
            return eventos.get(eventos.size() - 1);
        }
    }

    private ObservadorEspia espia;

    @BeforeEach
    void setUp() {
        espia = new ObservadorEspia();
    }

    private static Supplier<Mazo> mazoDe(Carta... cartas) {
        return () -> new Mazo(List.of(cartas));
    }

    private Partida partidaCon(Carta... cartas) throws RemoteException {
        Partida partida = new Partida(mazoDe(cartas));
        partida.agregarObservador(espia);
        partida.iniciar();
        return partida;
    }

    @Test
    void iniciarMuestraPrimeraCartaYNotifica() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        assertEquals(new Carta(5, ORO), partida.getCartaActual());
        assertEquals(0, partida.getPuntaje());
        assertEquals(2, partida.getCartasRestantes());
        assertEquals(List.of(Eventos.CARTA_NUEVA), espia.eventos);
    }

    @Test
    void aciertoSumaPunto() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(MAYOR);
        assertEquals(1, partida.getPuntaje());
        assertEquals(new Carta(7, COPA), partida.getCartaActual());
        assertFalse(partida.isTerminada());
        assertEquals(Eventos.CARTA_NUEVA, espia.ultimo());
    }

    @Test
    void errorTerminaPartida() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(MENOR);
        assertTrue(partida.isTerminada());
        assertFalse(partida.isGanada());
        assertEquals(0, partida.getPuntaje());
        assertEquals(Eventos.PARTIDA_TERMINADA, espia.ultimo());
    }

    @Test
    void empateEsError() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(5, COPA), new Carta(1, BASTO));
        partida.apostar(MAYOR);
        assertTrue(partida.isTerminada());
        assertEquals(0, partida.getPuntaje());
    }

    @Test
    void mazoAgotadoEsVictoria() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA));
        partida.apostar(MAYOR);
        assertTrue(partida.isTerminada());
        assertTrue(partida.isGanada());
        assertEquals(1, partida.getPuntaje());
        assertEquals(Eventos.PARTIDA_TERMINADA, espia.ultimo());
    }

    @Test
    void apostarConPartidaTerminadaFalla() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(MENOR);
        assertThrows(IllegalStateException.class, () -> partida.apostar(MAYOR));
    }

    @Test
    void abandonarConPartidaTerminadaFalla() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.abandonar();
        assertThrows(IllegalStateException.class, partida::abandonar);
    }

    @Test
    void abandonarTermina() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.abandonar();
        assertTrue(partida.isTerminada());
        assertFalse(partida.isGanada());
        assertEquals(Eventos.PARTIDA_TERMINADA, espia.ultimo());
    }

    @Test
    void nuevaPartidaReinicia() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(MAYOR);
        partida.apostar(MAYOR); // 2 < 7: pierde
        partida.iniciar();
        assertEquals(0, partida.getPuntaje());
        assertFalse(partida.isTerminada());
        assertEquals(new Carta(5, ORO), partida.getCartaActual());
        assertEquals(2, partida.getCartasRestantes());
    }

    @Test
    void removerObservadorDejaDeNotificar() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.removerObservador(espia);
        partida.apostar(MAYOR);
        assertEquals(1, espia.eventos.size());
    }

    @Test
    void cartaEsSerializable() {
        assertInstanceOf(Serializable.class, new Carta(1, ORO));
    }
}
