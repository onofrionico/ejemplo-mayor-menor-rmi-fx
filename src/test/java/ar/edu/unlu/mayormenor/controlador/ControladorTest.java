package ar.edu.unlu.mayormenor.controlador;

import ar.edu.unlu.mayormenor.modelo.Apuesta;
import ar.edu.unlu.mayormenor.modelo.Carta;
import ar.edu.unlu.mayormenor.modelo.Jugador;
import ar.edu.unlu.mayormenor.modelo.Mazo;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.vista.IVista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ar.edu.unlu.mayormenor.modelo.Apuesta.MAYOR;
import static ar.edu.unlu.mayormenor.modelo.Apuesta.MENOR;
import static ar.edu.unlu.mayormenor.modelo.Palo.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * El Controlador se prueba sin JavaFX ni red: en lugar de la vista gráfica
 * le damos una vista espía que anota lo que le piden mostrar, y en lugar del
 * stub del servidor, una Partida local.
 */
class ControladorTest {

    static class VistaEspia implements IVista {
        final List<String> llamadas = new ArrayList<>();

        private static String nombres(List<Jugador> jugadores) {
            return jugadores.stream().map(Jugador::nombre).collect(Collectors.joining(","));
        }

        @Override
        public void mostrarSala(List<Jugador> jugadores, Jugador admin, Jugador yo) {
            llamadas.add("mostrarSala " + nombres(jugadores) + " admin=" + admin.nombre() + " yo=" + yo.nombre());
        }

        @Override
        public void mostrarJuego() {
            llamadas.add("mostrarJuego");
        }

        @Override
        public void mostrarMesa(Carta carta, int restantes, List<Jugador> jugadores, Jugador deTurno, Jugador yo) {
            llamadas.add("mostrarMesa " + carta.numero() + "-" + carta.palo() + " r" + restantes
                    + " turno=" + deTurno.nombre() + " yo=" + yo.nombre() + (yo.vivo() ? "" : " eliminado"));
        }

        @Override
        public void mostrarResultado(List<Jugador> ganadores, List<Jugador> jugadores, Jugador yo, boolean soyAdmin) {
            llamadas.add("mostrarResultado ganadores=" + nombres(ganadores) + " yo=" + yo.nombre() + " admin=" + soyAdmin);
        }

        @Override
        public void mostrarError(String mensaje) {
            llamadas.add("mostrarError " + mensaje);
        }

        String ultima() {
            return llamadas.get(llamadas.size() - 1);
        }
    }

    /** Una partida que simula que se cortó la red. */
    static class PartidaSinConexion extends Partida {
        PartidaSinConexion() {
            super(() -> new Mazo(List.of(new Carta(5, ORO), new Carta(7, COPA))));
        }

        @Override
        public void apostar(int idJugador, Apuesta apuesta) throws RemoteException {
            throw new RemoteException("sin conexión");
        }
    }

    /** Conecta controlador y modelo igual que lo hace Cliente.iniciar(...) de la librería. */
    private static Controlador conectar(Partida partida, IVista vista) throws RemoteException {
        Controlador controlador = new Controlador();
        controlador.setModeloRemoto(partida);
        partida.agregarObservador(controlador);
        controlador.setVista(vista);
        return controlador;
    }

    private Partida partida;
    private VistaEspia vista;
    private Controlador controlador;

    @BeforeEach
    void setUp() throws RemoteException {
        partida = new Partida(() -> new Mazo(List.of(
                new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA))));
        vista = new VistaEspia();
        controlador = conectar(partida, vista);
    }

    // ----- Sala -----

    @Test
    void entrarMuestraLaSala() {
        assertTrue(controlador.entrar("Ana"));
        assertEquals(List.of("mostrarSala Ana admin=Ana yo=Ana"), vista.llamadas);
    }

    @Test
    void entrarConNombreRepetidoMuestraError() throws RemoteException {
        controlador.entrar("Ana");
        VistaEspia otraVista = new VistaEspia();
        Controlador otro = conectar(partida, otraVista);
        assertFalse(otro.entrar("Ana"));
        assertEquals("mostrarError Ya hay un jugador llamado Ana", otraVista.ultima());
    }

    @Test
    void laSalaSeActualizaCuandoEntraOtro() throws RemoteException {
        controlador.entrar("Ana");
        conectar(partida, new VistaEspia()).entrar("Beto");
        assertEquals("mostrarSala Ana,Beto admin=Ana yo=Ana", vista.ultima());
    }

    @Test
    void soloElAdminPuedeEmpezar() throws RemoteException {
        controlador.entrar("Ana");
        VistaEspia vistaBeto = new VistaEspia();
        Controlador beto = conectar(partida, vistaBeto);
        beto.entrar("Beto");
        beto.empezar();
        assertEquals("mostrarError Solo Ana puede empezar la partida", vistaBeto.ultima());
    }

    // ----- Mesa y turnos -----

    @Test
    void empezarMuestraLaMesaYLaPrimeraCarta() {
        controlador.entrar("Ana");
        controlador.empezar();
        assertEquals(List.of("mostrarSala Ana admin=Ana yo=Ana", "mostrarJuego", "mostrarMesa 5-ORO r2 turno=Ana yo=Ana"),
                vista.llamadas);
    }

    @Test
    void soloElJugadorDeTurnoVeSuTurno() throws RemoteException {
        VistaEspia vistaBeto = new VistaEspia();
        controlador.entrar("Ana");
        conectar(partida, vistaBeto).entrar("Beto");
        controlador.empezar();
        assertEquals("mostrarMesa 5-ORO r2 turno=Ana yo=Beto", vistaBeto.ultima());
        controlador.apostar(MAYOR);
        assertEquals("mostrarMesa 7-COPA r1 turno=Beto yo=Beto", vistaBeto.ultima());
        assertEquals("mostrarMesa 7-COPA r1 turno=Beto yo=Ana", vista.ultima());
    }

    @Test
    void apostarFueraDeTurnoMuestraError() throws RemoteException {
        VistaEspia vistaBeto = new VistaEspia();
        controlador.entrar("Ana");
        Controlador beto = conectar(partida, vistaBeto);
        beto.entrar("Beto");
        controlador.empezar();
        beto.apostar(MAYOR);
        assertEquals("mostrarError No es tu turno", vistaBeto.ultima());
    }

    @Test
    void elEliminadoSigueViendoLaMesa() throws RemoteException {
        VistaEspia vistaBeto = new VistaEspia();
        VistaEspia vistaCaro = new VistaEspia();
        controlador.entrar("Ana");
        conectar(partida, vistaBeto).entrar("Beto");
        conectar(partida, vistaCaro).entrar("Caro");
        controlador.empezar();
        controlador.apostar(MENOR); // 7 > 5: Ana queda eliminada
        assertEquals("mostrarMesa 7-COPA r1 turno=Beto yo=Ana eliminado", vista.ultima());
    }

    @Test
    void fallarMuestraElResultado() {
        controlador.entrar("Ana");
        controlador.empezar();
        controlador.apostar(MAYOR);
        controlador.apostar(MAYOR); // 2 < 7
        assertEquals("mostrarResultado ganadores= yo=Ana admin=true", vista.ultima());
    }

    @Test
    void elResultadoLlegaATodos() throws RemoteException {
        VistaEspia vistaBeto = new VistaEspia();
        controlador.entrar("Ana");
        conectar(partida, vistaBeto).entrar("Beto");
        controlador.empezar();
        controlador.apostar(MENOR); // Ana falla: gana Beto
        assertEquals("mostrarResultado ganadores=Beto yo=Beto admin=false", vistaBeto.ultima());
        assertEquals("mostrarResultado ganadores=Beto yo=Ana admin=true", vista.ultima());
    }

    @Test
    void abandonarMuestraElResultado() {
        controlador.entrar("Ana");
        controlador.empezar();
        controlador.abandonar();
        assertEquals("mostrarResultado ganadores= yo=Ana admin=true", vista.ultima());
    }

    @Test
    void volverALaSala() {
        controlador.entrar("Ana");
        controlador.empezar();
        controlador.abandonar();
        controlador.volverALaSala();
        assertEquals("mostrarSala Ana admin=Ana yo=Ana", vista.ultima());
    }

    @Test
    void errorDelModeloSeMuestraEnLaVista() throws RemoteException {
        Controlador conCorte = conectar(new PartidaSinConexion(), vista);
        conCorte.entrar("Ana");
        conCorte.empezar();
        conCorte.apostar(MAYOR);
        assertEquals("mostrarError sin conexión", vista.ultima());
    }
}
