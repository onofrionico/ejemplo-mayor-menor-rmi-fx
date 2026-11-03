package ar.edu.unlu.mayormenor.modelo;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.rmi.RemoteException;
import java.util.function.Supplier;

/**
 * Una partida de "Mayor o Menor".
 * <p>
 * Se muestra una carta y el jugador apuesta si la siguiente será mayor o menor.
 * Si acierta suma un punto; si falla (o sale el mismo número) la partida termina.
 * Si se acaba el mazo, el jugador ganó.
 * <p>
 * Es el <b>modelo</b>: no sabe nada de JavaFX. Ahora es un <b>observable remoto</b>: hereda de
 * {@link ObservableRemoto} (de la librería RMIMVC), que guarda la lista de observadores y les avisa
 * aunque estén en otra máquina.
 */
public class Partida extends ObservableRemoto implements IPartida {

    private final Supplier<Mazo> fabricaDeMazos;

    private Mazo mazo;
    private Carta cartaActual;
    private int puntaje;
    private boolean terminada;
    private boolean ganada;

    /** @param fabricaDeMazos da un mazo nuevo cada vez que empieza una partida */
    public Partida(Supplier<Mazo> fabricaDeMazos) {
        this.fabricaDeMazos = fabricaDeMazos;
    }

    /** Empieza (o reinicia) la partida con un mazo nuevo. */
    @Override
    public void iniciar() throws RemoteException {
        mazo = fabricaDeMazos.get();
        puntaje = 0;
        terminada = false;
        ganada = false;
        cartaActual = mazo.sacar();
        notificarObservadores(Eventos.CARTA_NUEVA);
    }

    @Override
    public void apostar(Apuesta apuesta) throws RemoteException {
        Eventos evento;
        verificarEnJuego();
        Carta siguiente = mazo.sacar();
        boolean acerto = apuesta == Apuesta.MAYOR
                ? siguiente.esMayorQue(cartaActual)
                : siguiente.esMenorQue(cartaActual);
        cartaActual = siguiente;
        if (!acerto) {
            evento = terminar(false);
        } else {
            puntaje++;
            evento = mazo.quedanCartas() ? Eventos.CARTA_NUEVA : terminar(true);
        }
        notificarObservadores(evento);
    }

    @Override
    public void abandonar() throws RemoteException {
        Eventos evento;
        verificarEnJuego();
        evento = terminar(false);
        notificarObservadores(evento);
    }

    private Eventos terminar(boolean gano) {
        terminada = true;
        ganada = gano;
        return Eventos.PARTIDA_TERMINADA;
    }

    private void verificarEnJuego() {
        if (mazo == null || terminada) {
            throw new IllegalStateException("La partida no está en juego");
        }
    }

    @Override
    public Carta getCartaActual() {
        return cartaActual;
    }

    @Override
    public int getPuntaje() {
        return puntaje;
    }

    @Override
    public int getCartasRestantes() {
        return mazo == null ? 0 : mazo.cantidad();
    }

    @Override
    public boolean isTerminada() {
        return terminada;
    }

    /** true solo si la partida terminó porque se acabó el mazo. */
    @Override
    public boolean isGanada() {
        return ganada;
    }
}
