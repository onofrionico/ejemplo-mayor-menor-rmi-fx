package ar.edu.unlu.mayormenor.observer;

/**
 * Quien quiere enterarse de los cambios de un {@link IObservable}.
 * Mismo contrato que vimos en la Clase 10.
 */
public interface IObservador {
    /**
     * @param observable quién cambió
     * @param evento     qué pasó (en este juego, un valor de {@code Eventos})
     */
    void actualizar(IObservable observable, Object evento);
}
