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
        partida.apostar(buscar(partida, "Ana").id(), MENOR); // 7 > 5: pierde
        partida.agregarJugador("Beto");
        assertEquals(2, partida.getJugadores().size());
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
        // Beto juega la próxima
        partida.iniciar(buscar(partida, "Ana").id());
        assertTrue(buscar(partida, "Beto").vivo());
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

    // ----- Turnos y eliminación -----

    /** Una partida ya empezada por el admin (el primero), con esos jugadores y ese mazo. */
    private Partida empezada(List<String> nombres, Carta... cartas) throws RemoteException {
        Partida partida = sala(cartas);
        for (String nombre : nombres) {
            partida.agregarJugador(nombre);
        }
        partida.iniciar(partida.getAdmin().id());
        return partida;
    }

    private static Jugador buscar(Partida partida, String nombre) {
        return partida.getJugadores().stream().filter(j -> j.nombre().equals(nombre)).findFirst().orElseThrow();
    }

    private static List<String> nombres(List<Jugador> jugadores) {
        return jugadores.stream().map(Jugador::nombre).toList();
    }

    @Test
    void empiezaElAdmin() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto"), new Carta(5, ORO), new Carta(7, COPA));
        assertEquals("Ana", partida.getJugadorDeTurno().nombre());
    }

    @Test
    void enLaSalaNoHayTurno() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO));
        partida.agregarJugador("Ana");
        assertNull(partida.getJugadorDeTurno());
    }

    @Test
    void turnoPasaAlSiguienteDespuesDeAcertar() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto"), new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(buscar(partida, "Ana").id(), MAYOR);
        assertEquals("Beto", partida.getJugadorDeTurno().nombre());
        assertEquals(1, buscar(partida, "Ana").aciertos());
        assertTrue(buscar(partida, "Ana").vivo());
        assertEquals(new Carta(7, COPA), partida.getCartaActual());
        assertEquals(Eventos.CARTA_NUEVA, espia.ultimo());
    }

    @Test
    void fallarEliminaYPasaElTurno() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto", "Caro"),
                new Carta(5, ORO), new Carta(7, COPA), new Carta(10, ESPADA));
        partida.apostar(buscar(partida, "Ana").id(), MENOR);
        assertFalse(buscar(partida, "Ana").vivo());
        assertEquals("Beto", partida.getJugadorDeTurno().nombre());
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstado());
    }

    @Test
    void mismoNumeroElimina() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto", "Caro"),
                new Carta(5, ORO), new Carta(5, COPA), new Carta(10, ESPADA));
        partida.apostar(buscar(partida, "Ana").id(), MAYOR);
        assertFalse(buscar(partida, "Ana").vivo());
    }

    @Test
    void apostarFueraDeTurnoSeRechaza() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto"), new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        int beto = buscar(partida, "Beto").id();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> partida.apostar(beto, MAYOR));
        assertEquals("No es tu turno", e.getMessage());
        assertEquals(new Carta(5, ORO), partida.getCartaActual());
    }

    @Test
    void eliminadoSeSaltaEnLosTurnos() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto", "Caro"), new Carta(5, ORO), new Carta(7, COPA),
                new Carta(10, ESPADA), new Carta(12, BASTO), new Carta(1, ORO));
        partida.apostar(buscar(partida, "Ana").id(), MENOR);   // 7 > 5: Ana queda eliminada
        partida.apostar(buscar(partida, "Beto").id(), MAYOR);  // 10 > 7
        partida.apostar(buscar(partida, "Caro").id(), MAYOR);  // 12 > 10
        assertEquals("Beto", partida.getJugadorDeTurno().nombre());
    }

    @Test
    void ganaElUltimoVivo() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto"), new Carta(5, ORO), new Carta(7, COPA), new Carta(10, ESPADA));
        partida.apostar(buscar(partida, "Ana").id(), MENOR);
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
        assertEquals(List.of("Beto"), nombres(partida.getGanadores()));
        assertNull(partida.getJugadorDeTurno());
        assertEquals(Eventos.PARTIDA_TERMINADA, espia.ultimo());
    }

    @Test
    void mazoVacioGananTodosLosVivos() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto"), new Carta(5, ORO), new Carta(7, COPA), new Carta(10, ESPADA));
        partida.apostar(buscar(partida, "Ana").id(), MAYOR);
        partida.apostar(buscar(partida, "Beto").id(), MAYOR);
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
        assertEquals(List.of("Ana", "Beto"), nombres(partida.getGanadores()));
    }

    @Test
    void unJugadorSigueConElTurnoSiAcierta() throws RemoteException {
        Partida partida = empezada(List.of("Ana"), new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(1, MAYOR);
        assertEquals("Ana", partida.getJugadorDeTurno().nombre());
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstado());
    }

    @Test
    void unJugadorQueFallaPierde() throws RemoteException {
        Partida partida = empezada(List.of("Ana"), new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(1, MENOR);
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
        assertTrue(partida.getGanadores().isEmpty());
    }

    @Test
    void unJugadorQueVaciaElMazoGana() throws RemoteException {
        Partida partida = empezada(List.of("Ana"), new Carta(5, ORO), new Carta(7, COPA));
        partida.apostar(1, MAYOR);
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
        assertEquals(List.of("Ana"), nombres(partida.getGanadores()));
        assertEquals(1, buscar(partida, "Ana").aciertos());
    }

    @Test
    void abandonarElimina() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto", "Caro"), new Carta(5, ORO), new Carta(7, COPA));
        partida.abandonar(buscar(partida, "Ana").id());
        assertFalse(buscar(partida, "Ana").vivo());
        assertEquals("Beto", partida.getJugadorDeTurno().nombre());
        assertEquals(Eventos.JUGADORES_CAMBIARON, espia.ultimo());
    }

    @Test
    void abandonarFueraDeTurnoNoCambiaElTurno() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto", "Caro"), new Carta(5, ORO), new Carta(7, COPA));
        partida.abandonar(buscar(partida, "Caro").id());
        assertFalse(buscar(partida, "Caro").vivo());
        assertEquals("Ana", partida.getJugadorDeTurno().nombre());
    }

    @Test
    void abandonarDejandoUnSoloVivoTermina() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto"), new Carta(5, ORO), new Carta(7, COPA));
        partida.abandonar(buscar(partida, "Ana").id());
        assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
        assertEquals(List.of("Beto"), nombres(partida.getGanadores()));
        assertEquals(Eventos.PARTIDA_TERMINADA, espia.ultimo());
    }

    @Test
    void unEliminadoNoPuedeAbandonar() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto", "Caro"),
                new Carta(5, ORO), new Carta(7, COPA), new Carta(10, ESPADA));
        int ana = buscar(partida, "Ana").id();
        partida.apostar(ana, MENOR);
        assertThrows(IllegalStateException.class, () -> partida.abandonar(ana));
    }

    @Test
    void apostarConPartidaTerminadaFalla() throws RemoteException {
        Partida partida = empezada(List.of("Ana"), new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        partida.apostar(1, MENOR);
        assertThrows(IllegalStateException.class, () -> partida.apostar(1, MAYOR));
    }

    @Test
    void apostarEnLaSalaFalla() throws RemoteException {
        Partida partida = sala(new Carta(5, ORO), new Carta(7, COPA));
        Jugador ana = partida.agregarJugador("Ana");
        assertThrows(IllegalStateException.class, () -> partida.apostar(ana.id(), MAYOR));
    }

    @Test
    void nuevaPartidaReviveATodos() throws RemoteException {
        Partida partida = empezada(List.of("Ana", "Beto"), new Carta(5, ORO), new Carta(7, COPA), new Carta(10, ESPADA));
        partida.apostar(buscar(partida, "Ana").id(), MAYOR);   // Ana acierta
        partida.apostar(buscar(partida, "Beto").id(), MENOR);  // Beto falla: gana Ana
        partida.iniciar(partida.getAdmin().id());
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstado());
        assertTrue(partida.getJugadores().stream().allMatch(Jugador::vivo));
        assertTrue(partida.getJugadores().stream().allMatch(j -> j.aciertos() == 0));
        assertEquals("Ana", partida.getJugadorDeTurno().nombre());
        assertEquals(new Carta(5, ORO), partida.getCartaActual());
        assertEquals(2, partida.getCartasRestantes());
        assertTrue(partida.getGanadores().isEmpty());
    }

    @Test
    void removerObservadorDejaDeNotificar() throws RemoteException {
        Partida partida = partidaCon(new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA));
        int antes = espia.eventos.size();
        partida.removerObservador(espia);
        partida.apostar(1, MAYOR);
        assertEquals(antes, espia.eventos.size());
    }

    @Test
    void cartaEsSerializable() {
        assertInstanceOf(Serializable.class, new Carta(1, ORO));
    }
}
