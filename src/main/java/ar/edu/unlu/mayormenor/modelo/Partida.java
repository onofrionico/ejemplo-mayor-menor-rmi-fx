package ar.edu.unlu.mayormenor.modelo;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Una partida de "Mayor o Menor" por turnos, para uno o más jugadores.
 * <p>
 * Los jugadores entran a una sala de espera; el primero es el admin y es quien la empieza.
 * Todos ven la misma carta. En su turno, cada jugador apuesta si la siguiente será mayor o menor:
 * si acierta sigue vivo; si falla (o sale el mismo número) queda eliminado. Después de cada
 * apuesta el turno pasa al siguiente jugador vivo.
 * <ul>
 *   <li>Gana el último que queda vivo.</li>
 *   <li>Si se acaba el mazo, ganan todos los que siguen vivos.</li>
 *   <li>Jugando de a uno, es el juego de la Clase 11: perdés al fallar y ganás si vaciás el mazo.</li>
 * </ul>
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
    /** Posición en {@link #jugadores} del jugador de turno. */
    private int turno;
    /** Cuántos jugadores había al empezar: define cuándo termina la partida. */
    private int jugadoresAlEmpezar;
    private List<Jugador> ganadores = List.of();

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
    public void quitarJugador(int idJugador) throws RemoteException {
        Eventos evento = Eventos.JUGADORES_CAMBIARON;
        int posicion = posicionDe(idJugador);
        jugadores.remove(posicion);
        if (jugadores.isEmpty()) {
            // No queda nadie: la próxima persona que entre arranca de cero
            estado = EstadoPartida.SALA;
        } else if (estado == EstadoPartida.EN_JUEGO) {
            if (terminoLaPartida()) {
                evento = Eventos.PARTIDA_TERMINADA;
            } else if (posicion < turno) {
                turno--;   // se corrió la lista: el de turno sigue siendo el mismo
            } else if (posicion == turno) {
                // Le tocaba al que se fue: ahora le toca al siguiente vivo (el que quedó en su lugar)
                turno = (posicion - 1 + jugadores.size()) % jugadores.size();
                pasarTurno();
            }
        }
        // El admin es siempre el primero de la lista: si se fue, ya pasó al siguiente
        notificarObservadores(evento);
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
        jugadores.replaceAll(j -> new Jugador(j.id(), j.nombre(), true, 0));
        jugadoresAlEmpezar = jugadores.size();
        ganadores = List.of();
        mazo = fabricaDeMazos.get();
        cartaActual = mazo.sacar();
        turno = 0;
        estado = EstadoPartida.EN_JUEGO;
        notificarObservadores(Eventos.PARTIDA_INICIADA);
    }

    @Override
    public void apostar(int idJugador, Apuesta apuesta) throws RemoteException {
        Eventos evento;
        verificarEnJuego();
        Jugador jugador = jugadores.get(turno);
        if (jugador.id() != idJugador) {
            throw new IllegalStateException("No es tu turno");
        }
        Carta siguiente = mazo.sacar();
        boolean acerto = apuesta == Apuesta.MAYOR
                ? siguiente.esMayorQue(cartaActual)
                : siguiente.esMenorQue(cartaActual);
        cartaActual = siguiente;
        jugadores.set(turno, acerto
                ? new Jugador(jugador.id(), jugador.nombre(), true, jugador.aciertos() + 1)
                : eliminado(jugador));
        if (terminoLaPartida()) {
            evento = Eventos.PARTIDA_TERMINADA;
        } else {
            pasarTurno();
            evento = Eventos.CARTA_NUEVA;
        }
        notificarObservadores(evento);
    }

    @Override
    public void abandonar(int idJugador) throws RemoteException {
        Eventos evento;
        verificarEnJuego();
        int posicion = posicionDe(idJugador);
        Jugador jugador = jugadores.get(posicion);
        if (!jugador.vivo()) {
            throw new IllegalStateException("Ya estás eliminado");
        }
        jugadores.set(posicion, eliminado(jugador));
        if (terminoLaPartida()) {
            evento = Eventos.PARTIDA_TERMINADA;
        } else {
            if (posicion == turno) {
                pasarTurno();
            }
            evento = Eventos.JUGADORES_CAMBIARON;
        }
        notificarObservadores(evento);
    }

    private static Jugador eliminado(Jugador j) {
        return new Jugador(j.id(), j.nombre(), false, j.aciertos());
    }

    /**
     * Si la partida terminó, la cierra y anota a los ganadores (los que siguen vivos).
     * Con 2 o más jugadores termina cuando queda uno solo; de a uno, cuando ese jugador falla.
     * En los dos casos también termina si se acabó el mazo.
     */
    private boolean terminoLaPartida() {
        List<Jugador> vivos = jugadores.stream().filter(Jugador::vivo).toList();
        int minimoParaSeguir = jugadoresAlEmpezar >= 2 ? 2 : 1;
        if (vivos.size() < minimoParaSeguir || !mazo.quedanCartas()) {
            estado = EstadoPartida.TERMINADA;
            ganadores = vivos;
            return true;
        }
        return false;
    }

    /** El turno pasa al siguiente jugador vivo, dando la vuelta a la mesa. */
    private void pasarTurno() {
        for (int i = 1; i <= jugadores.size(); i++) {
            int candidato = (turno + i) % jugadores.size();
            if (jugadores.get(candidato).vivo()) {
                turno = candidato;
                return;
            }
        }
    }

    private int posicionDe(int idJugador) {
        for (int i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).id() == idJugador) {
                return i;
            }
        }
        throw new IllegalArgumentException("No existe el jugador " + idJugador);
    }

    private void verificarEnJuego() {
        if (estado != EstadoPartida.EN_JUEGO) {
            throw new IllegalStateException("La partida no está en juego");
        }
    }

    @Override
    public Jugador getJugadorDeTurno() {
        return estado == EstadoPartida.EN_JUEGO ? jugadores.get(turno) : null;
    }

    @Override
    public List<Jugador> getGanadores() {
        return ganadores;
    }

    @Override
    public Carta getCartaActual() {
        return cartaActual;
    }

    @Override
    public int getCartasRestantes() {
        return mazo == null ? 0 : mazo.cantidad();
    }
}
