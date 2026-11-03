package ar.edu.unlu.mayormenor.modelo;

import ar.edu.unlu.mayormenor.observer.IObservable;
import ar.edu.unlu.mayormenor.observer.IObservador;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Una partida de "Mayor o Menor".
 * <p>
 * Se muestra una carta y el jugador apuesta si la siguiente será mayor o menor.
 * Si acierta suma un punto; si falla (o sale el mismo número) la partida termina.
 * Si se acaba el mazo, el jugador ganó.
 * <p>
 * Es el <b>modelo</b>: no sabe nada de JavaFX. Cuando cambia, avisa a sus observadores.
 */
public class Partida implements IObservable {

    private final Supplier<Mazo> fabricaDeMazos;
    private final List<IObservador> observadores = new ArrayList<>();

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
    public void iniciar() {
        mazo = fabricaDeMazos.get();
        puntaje = 0;
        terminada = false;
        ganada = false;
        cartaActual = mazo.sacar();
        notificarObservadores(Eventos.CARTA_NUEVA);
    }

    public void apostar(Apuesta apuesta) {
        verificarEnJuego();
        Carta siguiente = mazo.sacar();
        boolean acerto = apuesta == Apuesta.MAYOR
                ? siguiente.esMayorQue(cartaActual)
                : siguiente.esMenorQue(cartaActual);
        cartaActual = siguiente;
        if (!acerto) {
            terminar(false);
            return;
        }
        puntaje++;
        if (mazo.quedanCartas()) {
            notificarObservadores(Eventos.CARTA_NUEVA);
        } else {
            terminar(true);
        }
    }

    public void abandonar() {
        verificarEnJuego();
        terminar(false);
    }

    private void terminar(boolean gano) {
        terminada = true;
        ganada = gano;
        notificarObservadores(Eventos.PARTIDA_TERMINADA);
    }

    private void verificarEnJuego() {
        if (mazo == null || terminada) {
            throw new IllegalStateException("La partida no está en juego");
        }
    }

    public Carta getCartaActual() {
        return cartaActual;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public int getCartasRestantes() {
        return mazo == null ? 0 : mazo.cantidad();
    }

    public boolean isTerminada() {
        return terminada;
    }

    /** true solo si la partida terminó porque se acabó el mazo. */
    public boolean isGanada() {
        return ganada;
    }

    @Override
    public void agregarObservador(IObservador o) {
        observadores.add(o);
    }

    @Override
    public void removerObservador(IObservador o) {
        observadores.remove(o);
    }

    @Override
    public void notificarObservadores(Object evento) {
        for (IObservador o : List.copyOf(observadores)) {
            o.actualizar(this, evento);
        }
    }
}
