package ar.edu.unlu.mayormenor.controlador;

import ar.edu.unlu.mayormenor.modelo.Apuesta;
import ar.edu.unlu.mayormenor.modelo.Eventos;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.observer.IObservable;
import ar.edu.unlu.mayormenor.observer.IObservador;
import ar.edu.unlu.mayormenor.vista.IVista;

/**
 * El Controlador del MVC.
 * <ul>
 *   <li>La vista le avisa lo que hizo el usuario ({@link #jugar()}, {@link #apostar(Apuesta)}...),
 *       y él se lo pide al modelo.</li>
 *   <li>Observa al modelo: cuando el modelo cambia, le pide a la vista que muestre el nuevo estado.</li>
 * </ul>
 * No importa nada de JavaFX: por eso se puede testear con una vista espía.
 */
public class Controlador implements IObservador {

    private final Partida modelo;
    private IVista vista;

    public Controlador(Partida modelo) {
        this.modelo = modelo;
        this.modelo.agregarObservador(this);
    }

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    public void jugar() {
        vista.mostrarJuego();
        modelo.iniciar();
    }

    public void apostar(Apuesta apuesta) {
        modelo.apostar(apuesta);
    }

    public void abandonar() {
        modelo.abandonar();
    }

    public void volverAlMenu() {
        vista.mostrarMenu();
    }

    @Override
    public void actualizar(IObservable observable, Object evento) {
        if (evento == Eventos.CARTA_NUEVA) {
            vista.mostrarCarta(modelo.getCartaActual(), modelo.getPuntaje(), modelo.getCartasRestantes());
        } else if (evento == Eventos.PARTIDA_TERMINADA) {
            vista.mostrarResultado(modelo.getPuntaje(), modelo.isGanada());
        }
    }
}
