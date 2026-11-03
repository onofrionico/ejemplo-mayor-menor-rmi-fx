package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.modelo.Carta;
import ar.edu.unlu.mayormenor.modelo.Jugador;

import java.util.List;

/**
 * Lo que el Controlador le puede pedir a la vista.
 * <p>
 * El Controlador solo conoce esta interfaz: no sabe si del otro lado hay JavaFX,
 * una consola o (en los tests) una vista espía.
 */
public interface IVista {
    /**
     * La sala de espera.
     *
     * @param admin quien puede empezar la partida
     * @param yo    el jugador de este cliente
     */
    void mostrarSala(List<Jugador> jugadores, Jugador admin, Jugador yo);

    void mostrarJuego();

    void mostrarCarta(Carta carta, int puntaje, int restantes);

    void mostrarResultado(int puntaje, boolean ganada);

    /** Algo salió mal (por ejemplo, se cortó la conexión con el servidor). */
    void mostrarError(String mensaje);
}
