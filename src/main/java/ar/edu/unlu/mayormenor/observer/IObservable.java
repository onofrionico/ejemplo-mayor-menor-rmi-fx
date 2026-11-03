package ar.edu.unlu.mayormenor.observer;

/**
 * Objeto que avisa a sus observadores cuando cambia su estado.
 * Mismo contrato que vimos en la Clase 10.
 */
public interface IObservable {
    void agregarObservador(IObservador o);

    void removerObservador(IObservador o);

    void notificarObservadores(Object evento);
}
