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

    private Partida sala(Carta... cartas) throws RemoteException {
        Partida partida = new Partida(mazoDe(cartas));
        partida.agregarObservador(espia);
        return partida;
    }

    /** Una partida de un solo jugador (Ana, id 1) ya empezada. */
    private Partida partidaCon(Carta... cartas) throws RemoteException {
        Partida partida = sala(cartas);
        Jugador ana = partida.agregarJugador("Ana");
        partida.iniciar(ana.id());
        return partida;
    }

    // ----- Sala de espera -----

    @Test
    void primeroEsAdmin() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO));
        Jugador ana = partida.agregarJugador("Ana");
        partida.agregarJugador("Beto");
        assertEquals(ana.id(), partida.getAdmin().id());
        assertEquals(List.of("Ana", "Beto"), partida.getJugadores().stream().map(Jugador::nombre).toList());
        assertEquals(EstadoPartida.SALA, partida.getEstado());
    }

    @Test
    void agregarNotificaJugadoresCambiaron() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO));
        partida.agregarJugador("Ana");
        assertEquals(List.of(Eventos.JUGADORES_CAMBIARON), espia.eventos);
    }

    @Test
    void nombreVacioSeRechaza() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> partida.agregarJugador("   "));
        assertEquals("Elegí un nombre", e.getMessage());
        assertTrue(partida.getJugadores().isEmpty());
    }

    @Test
    void nombreRepetidoSeRechaza() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO));
        partida.agregarJugador("Ana");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> partida.agregarJugador(" ana "));
        assertEquals("Ya hay un jugador llamado ana", e.getMessage());
        assertEquals(1, partida.getJugadores().size());
    }

    @Test
    void noSePuedeEntrarConLaPartidaEmpezada() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA));
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> partida.agregarJugador("Beto"));
        assertEquals("La partida ya empezó", e.getMessage());
    }

    @Test
    void conLaPartidaTerminadaSePuedeEntrarParaLaProxima() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(MENOR); // 7 > 5: pierde
        partida.agregarJugador("Beto");
        assertEquals(2, partida.getJugadores().size());
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
    }

    @Test
    void soloElAdminPuedeIniciar() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO), new Carta(7, COPA));
        partida.agregarJugador("Ana");
        Jugador beto = partida.agregarJugador("Beto");
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> partida.iniciar(beto.id()));
        assertEquals("Solo Ana puede empezar la partida", e.getMessage());
        assertEquals(EstadoPartida.SALA, partida.getEstado());
    }

    @Test
    void unSoloJugadorPuedeIniciar() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstado());
        assertEquals(new Carta(5, ORO), partida.getCartaActual());
        assertEquals(2, partida.getCartasRestantes());
        assertEquals(List.of(Eventos.JUGADORES_CAMBIARON, Eventos.PARTIDA_INICIADA), espia.eventos);
    }

    @Test
    void jugadorEsSerializable() {
        assertInstanceOf(Serializable.class, new Jugador(1, "Ana", true, 0));
    }

    // ----- El juego (todavía sin turnos) -----

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
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
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
    void apostarEnLaSalaFalla() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO), new Carta(7, COPA));
        partida.agregarJugador("Ana");
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
        partida.iniciar(partida.getAdmin().id());
        assertEquals(0, partida.getPuntaje());
        assertFalse(partida.isTerminada());
        assertEquals(new Carta(5, ORO), partida.getCartaActual());
        assertEquals(2, partida.getCartasRestantes());
    }

    @Test
    void removerObservadorDejaDeNotificar() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        int antes = espia.eventos.size();
        partida.removerObservador(espia);
        partida.apostar(MAYOR);
        assertEquals(antes, espia.eventos.size());
    }

    @Test
    void cartaEsSerializable() {
        assertInstanceOf(Serializable.class, new Carta(1, ORO));
    }
}
