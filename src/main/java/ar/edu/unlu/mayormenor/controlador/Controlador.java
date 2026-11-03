package ar.edu.unlu.mayormenor.controlador;

import ar.edu.unlu.mayormenor.modelo.Apuesta;
import ar.edu.unlu.mayormenor.modelo.Eventos;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.vista.IVista;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;

import java.rmi.RemoteException;

/**
 * El Controlador del MVC.
 * <ul>
 *   <li>La vista le avisa lo que hizo el usuario ({@link #jugar()}, {@link #apostar(Apuesta)}...),
 *       y él se lo pide al modelo.</li>
 *   <li>Observa al modelo: cuando el modelo cambia, le pide a la vista que muestre el nuevo estado.</li>
 * </ul>
 * Ahora el modelo es un observable remoto, así que el Controlador implementa
 * {@link IObservadorRemoto} y cada llamada al modelo puede tirar {@link RemoteException}.
 * No importa nada de JavaFX: por eso se puede testear con una vista espía.
 */
public class Controlador implements IObservadorRemoto {

    private final Partida modelo;
    private IVista vista;

    public Controlador(Partida modelo) {
        this.modelo = modelo;
        try {
            this.modelo.agregarObservador(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    public void jugar() {
        vista.mostrarJuego();
        try {
            modelo.iniciar();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void apostar(Apuesta apuesta) {
        try {
            modelo.apostar(apuesta);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void abandonar() {
        try {
            modelo.abandonar();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void volverAlMenu() {
        vista.mostrarMenu();
    }

    @Override
    public void actualizar(IObservableRemoto observable, Object evento) throws RemoteException {
        if (evento == Eventos.CARTA_NUEVA) {
            vista.mostrarCarta(modelo.getCartaActual(), modelo.getPuntaje(), modelo.getCartasRestantes());
        } else if (evento == Eventos.PARTIDA_TERMINADA) {
            vista.mostrarResultado(modelo.getPuntaje(), modelo.isGanada());
        }
    }
}
