package ar.edu.unlu.mayormenor.modelo;

import java.io.Serializable;

/**
 * Un jugador de la partida, tal como lo ve un cliente.
 * <p>
 * Es {@link Serializable} porque viaja por la red, y es inmutable: cuando algo cambia, la
 * {@link Partida} reemplaza el jugador por uno nuevo. Por eso, para saber si dos jugadores son
 * el mismo, se compara el {@link #id()} (y no {@code equals}, que mira también el estado).
 *
 * @param id       número único, en orden de llegada (1, 2, 3...)
 * @param nombre   el nombre que eligió al entrar
 * @param vivo     false cuando quedó eliminado
 * @param aciertos cuántas veces acertó en esta partida
 */
public record Jugador(int id, String nombre, boolean vivo, int aciertos) implements Serializable {

    public boolean esElMismoQue(Jugador otro) {
        return otro != null && id == otro.id;
    }
}
