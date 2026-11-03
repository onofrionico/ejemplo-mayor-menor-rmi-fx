package ar.edu.unlu.mayormenor.modelo;

/** Los cambios que la {@link Partida} notifica a sus observadores. */
public enum Eventos {
    /** Hay una nueva carta actual (al iniciar o después de un acierto). */
    CARTA_NUEVA,
    /** La partida terminó: por un error, por abandono o porque se acabó el mazo. */
    PARTIDA_TERMINADA
}
