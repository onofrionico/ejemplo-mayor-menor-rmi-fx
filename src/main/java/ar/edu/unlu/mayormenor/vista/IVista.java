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

    /** Pasa a la mesa de juego. */
    void mostrarJuego();

    /**
     * El estado de la mesa: la carta, quién está vivo y a quién le toca.
     * La vista habilita las apuestas solo si {@code deTurno} es {@code yo}.
     */
    void mostrarMesa(Carta carta, int restantes, List<Jugador> jugadores, Jugador deTurno, Jugador yo);

    /**
     * Fin de la partida.
     *
     * @param ganadores vacía si nadie ganó
     * @param soyAdmin  si este cliente puede empezar otra partida
     */
    void mostrarResultado(List<Jugador> ganadores, List<Jugador> jugadores, Jugador yo, boolean soyAdmin);

    /** Algo salió mal (por ejemplo, se cortó la conexión con el servidor). */
    void mostrarError(String mensaje);
}
