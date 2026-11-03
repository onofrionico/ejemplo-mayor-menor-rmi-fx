package ar.edu.unlu.mayormenor.modelo;

/** En qué momento está la partida. */
public enum EstadoPartida {
    /** Los jugadores van entrando; todavía no se juega. */
    SALA,
    /** Se está jugando. */
    EN_JUEGO,
    /** Terminó; el admin puede empezar otra con los mismos jugadores. */
    TERMINADA
}
