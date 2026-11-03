package ar.edu.unlu.mayormenor.modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;

/**
 * Lo que un cliente le puede pedir a la {@link Partida} a través de la red.
 * <p>
 * Es una interfaz <b>remota</b>: extiende {@link IObservableRemoto} (que a su vez extiende
 * {@code java.rmi.Remote}) y todos sus métodos declaran {@link RemoteException}, porque
 * cualquier llamada puede fallar por la red. El cliente nunca ve la clase {@code Partida}:
 * recibe un <i>stub</i> que implementa esta interfaz y reenvía cada llamada al servidor.
 */
public interface IPartida extends IObservableRemoto {

    void iniciar() throws RemoteException;

    void apostar(Apuesta apuesta) throws RemoteException;

    void abandonar() throws RemoteException;

    Carta getCartaActual() throws RemoteException;

    int getPuntaje() throws RemoteException;

    int getCartasRestantes() throws RemoteException;

    boolean isTerminada() throws RemoteException;

    boolean isGanada() throws RemoteException;
}
