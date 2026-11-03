package ar.edu.unlu.mayormenor.modelo;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Una partida de "Mayor o Menor".
 * <p>
 * Los jugadores entran a una sala de espera; el primero es el admin y es quien la empieza.
 * Se muestra una carta y se apuesta si la siguiente será mayor o menor.
 * Si se acierta se suma un punto; si se falla (o sale el mismo número) la partida termina.
 * Si se acaba el mazo, se ganó.
 * <p>
 * Es el <b>modelo</b>: no sabe nada de JavaFX. Es un <b>observable remoto</b>: hereda de
 * {@link ObservableRemoto} (de la librería RMIMVC), que guarda la lista de observadores y les avisa
 * aunque estén en otra máquina.
 */
public class Partida extends ObservableRemoto implements IPartida {

    private final Supplier<Mazo> fabricaDeMazos;
    private final List<Jugador> jugadores = new ArrayList<>();
    private int proximoId = 1;
    private EstadoPartida estado = EstadoPartida.SALA;

    private Mazo mazo;
    private Carta cartaActual;
    private int puntaje;
    private boolean ganada;

    /** @param fabricaDeMazos da un mazo nuevo cada vez que empieza una partida */
    public Partida(Supplier<Mazo> fabricaDeMazos) {
        this.fabricaDeMazos = fabricaDeMazos;
    }

    // ----- Sala de espera -----

    @Override
    public Jugador agregarJugador(String nombre) throws RemoteException {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("Elegí un nombre");
        }
        // Con la partida terminada sí se puede entrar: juega en la próxima
        if (estado == EstadoPartida.EN_JUEGO) {
            throw new IllegalStateException("La partida ya empezó");
        }
        for (Jugador j : jugadores) {
            if (j.nombre().equalsIgnoreCase(limpio)) {
                throw new IllegalArgumentException("Ya hay un jugador llamado " + limpio);
            }
        }
        Jugador nuevo = new Jugador(proximoId++, limpio, true, 0);
        jugadores.add(nuevo);
        notificarObservadores(Eventos.JUGADORES_CAMBIARON);
        return nuevo;
    }

    @Override
    public List<Jugador> getJugadores() {
        return List.copyOf(jugadores);
    }

    @Override
    public Jugador getAdmin() {
        return jugadores.isEmpty() ? null : jugadores.get(0);
    }

    @Override
    public EstadoPartida getEstado() {
        return estado;
    }

    // ----- El juego -----

    @Override
    public void iniciar(int idJugador) throws RemoteException {
        Jugador admin = getAdmin();
        if (admin == null || admin.id() != idJugador) {
            throw new IllegalStateException(admin == null
                    ? "No hay jugadores"
                    : "Solo " + admin.nombre() + " puede empezar la partida");
        }
        if (estado == EstadoPartida.EN_JUEGO) {
            throw new IllegalStateException("La partida ya empezó");
        }
        mazo = fabricaDeMazos.get();
        puntaje = 0;
        ganada = false;
        cartaActual = mazo.sacar();
        estado = EstadoPartida.EN_JUEGO;
        notificarObservadores(Eventos.PARTIDA_INICIADA);
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
        estado = EstadoPartida.TERMINADA;
        ganada = gano;
        return Eventos.PARTIDA_TERMINADA;
    }

    private void verificarEnJuego() {
        if (estado != EstadoPartida.EN_JUEGO) {
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
        return estado == EstadoPartida.TERMINADA;
    }

    /** true solo si la partida terminó porque se acabó el mazo. */
    @Override
    public boolean isGanada() {
        return ganada;
    }
}
