package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.modelo.Carta;

/**
 * Lo que el Controlador le puede pedir a la vista.
 * <p>
 * El Controlador solo conoce esta interfaz: no sabe si del otro lado hay JavaFX,
 * una consola o (en los tests) una vista espía.
 */
public interface IVista {
    void mostrarMenu();

    void mostrarJuego();

    void mostrarCarta(Carta carta, int puntaje, int restantes);

    void mostrarResultado(int puntaje, boolean ganada);
}
