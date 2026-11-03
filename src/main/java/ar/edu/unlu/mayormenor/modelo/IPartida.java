package ar.edu.unlu.mayormenor.modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Lo que un cliente le puede pedir a la {@link Partida} a través de la red.
 * <p>
 * Es una interfaz <b>remota</b>: extiende {@link IObservableRemoto} (que a su vez extiende
 * {@code java.rmi.Remote}) y todos sus métodos declaran {@link RemoteException}, porque
 * cualquier llamada puede fallar por la red. El cliente nunca ve la clase {@code Partida}:
 * recibe un <i>stub</i> que implementa esta interfaz y reenvía cada llamada al servidor.
 */
public interface IPartida extends IObservableRemoto {

    /**
     * Entra a la sala de espera. El primero que entra es el admin.
     *
     * @return el jugador creado (con su id): el cliente lo guarda para saber quién es
     * @throws IllegalArgumentException si el nombre está vacío o ya lo usa otro jugador
     * @throws IllegalStateException    si la partida ya empezó
     */
    Jugador agregarJugador(String nombre) throws RemoteException;

    /** Los jugadores, en orden de llegada. */
    List<Jugador> getJugadores() throws RemoteException;

    /** El primero en llegar; null si no hay nadie. */
    Jugador getAdmin() throws RemoteException;

    EstadoPartida getEstado() throws RemoteException;

    /**
     * Empieza (o vuelve a empezar) la partida con un mazo nuevo.
     *
     * @throws IllegalStateException si quien lo pide no es el admin, o si ya se está jugando
     */
    void iniciar(int idJugador) throws RemoteException;

    void apostar(Apuesta apuesta) throws RemoteException;

    void abandonar() throws RemoteException;

    Carta getCartaActual() throws RemoteException;

    int getPuntaje() throws RemoteException;

    int getCartasRestantes() throws RemoteException;

    boolean isTerminada() throws RemoteException;

    boolean isGanada() throws RemoteException;
}
