package ar.edu.unlu.mayormenor.controlador;

import ar.edu.unlu.mayormenor.modelo.Apuesta;
import ar.edu.unlu.mayormenor.modelo.Carta;
import ar.edu.unlu.mayormenor.modelo.Mazo;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.vista.IVista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static ar.edu.unlu.mayormenor.modelo.Apuesta.MAYOR;
import static ar.edu.unlu.mayormenor.modelo.Palo.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El Controlador se prueba sin JavaFX: en lugar de la vista gráfica
 * le damos una vista espía que anota lo que le piden mostrar.
 */
class ControladorTest {

    static class VistaEspia implements IVista {
        final List<String> llamadas = new ArrayList<>();

        @Override
        public void mostrarMenu() {
            llamadas.add("mostrarMenu");
        }

        @Override
        public void mostrarJuego() {
            llamadas.add("mostrarJuego");
        }

        @Override
        public void mostrarCarta(Carta carta, int puntaje, int restantes) {
            llamadas.add("mostrarCarta " + carta.numero() + "-" + carta.palo() + " " + puntaje + " " + restantes);
        }

        @Override
        public void mostrarResultado(int puntaje, boolean ganada) {
            llamadas.add("mostrarResultado " + puntaje + " " + ganada);
        }

        @Override
        public void mostrarError(String mensaje) {
            llamadas.add("mostrarError " + mensaje);
        }

        String ultima() {
            return llamadas.get(llamadas.size() - 1);
        }
    }

    private VistaEspia vista;
    private Controlador controlador;

    /** Una partida que simula que se cortó la red. */
    static class PartidaSinConexion extends Partida {
        PartidaSinConexion() {
            super(() -> new Mazo(List.of(new Carta(5, ORO), new Carta(7, COPA))));
        }

        @Override
        public void apostar(Apuesta apuesta) throws RemoteException {
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

    @BeforeEach
    void setUp() throws RemoteException {
        Partida partida = new Partida(() -> new Mazo(List.of(
                new Carta(5, ORO), new Carta(7, COPA), new Carta(2, ESPADA))));
        vista = new VistaEspia();
        controlador = conectar(partida, vista);
    }

    @Test
    void jugarMuestraLaMesaYLaPrimeraCarta() {
        controlador.jugar();
        assertEquals(List.of("mostrarJuego", "mostrarCarta 5-ORO 0 2"), vista.llamadas);
    }

    @Test
    void acertarMuestraLaNuevaCarta() {
        controlador.jugar();
        controlador.apostar(MAYOR);
        assertEquals("mostrarCarta 7-COPA 1 1", vista.ultima());
    }

    @Test
    void fallarMuestraElResultado() {
        controlador.jugar();
        controlador.apostar(MAYOR);
        controlador.apostar(MAYOR); // 2 < 7
        assertEquals("mostrarResultado 1 false", vista.ultima());
    }

    @Test
    void abandonarMuestraElResultado() {
        controlador.jugar();
        controlador.abandonar();
        assertEquals("mostrarResultado 0 false", vista.ultima());
    }

    @Test
    void volverAlMenu() {
        controlador.jugar();
        controlador.abandonar();
        controlador.volverAlMenu();
        assertEquals("mostrarMenu", vista.ultima());
    }

    @Test
    void errorDelModeloSeMuestraEnLaVista() throws RemoteException {
        Controlador conCorte = conectar(new PartidaSinConexion(), vista);
        conCorte.jugar();
        conCorte.apostar(MAYOR);
        assertTrue(vista.ultima().startsWith("mostrarError"), vista.ultima());
    }
}
