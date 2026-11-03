package ar.edu.unlu.mayormenor.controlador;

import ar.edu.unlu.mayormenor.modelo.Apuesta;
import ar.edu.unlu.mayormenor.modelo.Eventos;
import ar.edu.unlu.mayormenor.modelo.IPartida;
import ar.edu.unlu.mayormenor.modelo.Jugador;
import ar.edu.unlu.mayormenor.vista.IVista;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;

/**
 * El Controlador del MVC.
 * <ul>
 *   <li>La vista le avisa lo que hizo el usuario ({@link #entrar}, {@link #empezar()},
 *       {@link #apostar(Apuesta)}...), y él se lo pide al modelo.</li>
 *   <li>Observa al modelo: cuando el modelo cambia, le pide a la vista que muestre el nuevo estado.</li>
 * </ul>
 * Es un <b>controlador remoto</b> ({@link IControladorRemoto}): se crea sin modelo y es la
 * librería la que se lo entrega con {@link #setModeloRemoto} al conectarse al servidor.
 * Solo conoce la interfaz {@link IPartida}: del otro lado puede haber una {@code Partida} local
 * (en los tests) o un <i>stub</i> que habla con el servidor.
 * <p>
 * Hay un Controlador por cliente, y cada uno recuerda <b>quién es</b> ({@link #yo}): el jugador que
 * le devolvió el modelo al entrar.
 * <p>
 * Cualquier llamada al modelo puede fallar ({@link RemoteException} si se cortó la red,
 * {@link IllegalStateException} o {@link IllegalArgumentException} si el modelo no la permite):
 * el error se muestra en la vista.
 * No importa nada de JavaFX: por eso se puede testear con una vista espía.
 */
public class Controlador implements IControladorRemoto {

    private IPartida modelo;
    private IVista vista;
    /** El jugador de este cliente: el que nos devolvió el modelo al entrar. */
    private Jugador yo;

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    /** Lo llama la librería (Cliente.iniciar) con el stub del modelo que está en el servidor. */
    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modelo) throws RemoteException {
        this.modelo = (IPartida) modelo;
    }

    /**
     * Entra a la sala de espera con ese nombre.
     *
     * @return true si entró; false si el modelo lo rechazó (el error ya se mostró en la vista)
     */
    public boolean entrar(String nombre) {
        try {
            yo = modelo.agregarJugador(nombre);
            mostrarSala();
            return true;
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return false;
        }
    }

    /** Lo usa el botón "Empezar partida" (y "Jugar de nuevo"): solo funciona para el admin. */
    public void empezar() {
        try {
            modelo.iniciar(yo.id());
        } catch (RemoteException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void apostar(Apuesta apuesta) {
        try {
            modelo.apostar(apuesta);
        } catch (RemoteException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void abandonar() {
        try {
            modelo.abandonar();
        } catch (RemoteException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void volverALaSala() {
        try {
            mostrarSala();
        } catch (RemoteException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    private void mostrarSala() throws RemoteException {
        vista.mostrarSala(modelo.getJugadores(), modelo.getAdmin(), yo);
    }

    @Override
    public void actualizar(IObservableRemoto observable, Object evento) throws RemoteException {
        if (yo == null) {
            // Todavía no entramos a la sala (el aviso es por nuestra propia llegada, o de otro antes que nosotros)
            return;
        }
        if (evento == Eventos.JUGADORES_CAMBIARON) {
            mostrarSala();
        } else if (evento == Eventos.PARTIDA_INICIADA) {
            vista.mostrarJuego();
            vista.mostrarCarta(modelo.getCartaActual(), modelo.getPuntaje(), modelo.getCartasRestantes());
        } else if (evento == Eventos.CARTA_NUEVA) {
            vista.mostrarCarta(modelo.getCartaActual(), modelo.getPuntaje(), modelo.getCartasRestantes());
        } else if (evento == Eventos.PARTIDA_TERMINADA) {
            vista.mostrarResultado(modelo.getPuntaje(), modelo.isGanada());
        }
    }
}
