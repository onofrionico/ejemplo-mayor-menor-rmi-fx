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
 * <p>
 * Las acciones de un jugador reciben su id: así el modelo sabe quién la pide y puede
 * rechazarla (por ejemplo, si no es su turno).
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

    /**
     * El jugador se va (cerró la ventana). Si era el admin, el admin pasa al siguiente en orden
     * de llegada; si tenía el turno, pasa al siguiente vivo; si queda uno solo vivo, termina.
     */
    void quitarJugador(int idJugador) throws RemoteException;

    /** Los jugadores, en orden de llegada (que es también el orden de los turnos). */
    List<Jugador> getJugadores() throws RemoteException;

    /** El primero en llegar; null si no hay nadie. */
    Jugador getAdmin() throws RemoteException;

    EstadoPartida getEstado() throws RemoteException;

    /**
     * Empieza (o vuelve a empezar) la partida con un mazo nuevo: todos vivos y empieza el admin.
     *
     * @throws IllegalStateException si quien lo pide no es el admin, o si ya se está jugando
     */
    void iniciar(int idJugador) throws RemoteException;

    /**
     * El jugador de turno apuesta sobre la próxima carta. Si falla queda eliminado.
     * Después, el turno pasa al siguiente jugador vivo.
     *
     * @throws IllegalStateException si no se está jugando o no es el turno de ese jugador
     */
    void apostar(int idJugador, Apuesta apuesta) throws RemoteException;

    /**
     * El jugador se retira: queda eliminado (aunque no sea su turno).
     *
     * @throws IllegalStateException si no se está jugando o ya estaba eliminado
     */
    void abandonar(int idJugador) throws RemoteException;

    /** A quién le toca; null si no se está jugando. */
    Jugador getJugadorDeTurno() throws RemoteException;

    /** Los que ganaron la última partida (vacía si nadie ganó o si todavía no terminó). */
    List<Jugador> getGanadores() throws RemoteException;

    Carta getCartaActual() throws RemoteException;

    int getCartasRestantes() throws RemoteException;
}
