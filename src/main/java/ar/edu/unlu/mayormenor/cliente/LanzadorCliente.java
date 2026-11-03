package ar.edu.unlu.mayormenor.cliente;

import javafx.application.Application;

/**
 * Clase para correr un cliente con el botón "Run" de IntelliJ.
 * <p>
 * Si corrés directamente una clase que extiende {@code Application}, Java puede quejarse con
 * "JavaFX runtime components are missing". Arrancar desde una clase que NO extiende
 * {@code Application} evita ese chequeo.
 * <p>
 * Podés abrir varios clientes a la vez: cada uno es un jugador.
 */
public class LanzadorCliente {
    public static void main(String[] args) {
        Application.launch(AppCliente.class, args);
    }
}
