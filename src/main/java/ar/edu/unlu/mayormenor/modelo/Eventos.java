package ar.edu.unlu.mayormenor.modelo;

/** Los cambios que la {@link Partida} notifica a sus observadores. */
public enum Eventos {
    /** Entró (o salió) un jugador. */
    JUGADORES_CAMBIARON,
    /** El admin empezó la partida: todos pasan a la mesa. */
    PARTIDA_INICIADA,
    /** Hay una nueva carta actual (después de una apuesta). */
    CARTA_NUEVA,
    /** La partida terminó: por un error, por abandono o porque se acabó el mazo. */
    PARTIDA_TERMINADA
}
