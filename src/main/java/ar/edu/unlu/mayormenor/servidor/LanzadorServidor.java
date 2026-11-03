package ar.edu.unlu.mayormenor.servidor;

import javafx.application.Application;

/**
 * Clase para correr el servidor con el botón "Run" de IntelliJ.
 * <p>
 * Si corrés directamente una clase que extiende {@code Application}, Java puede quejarse con
 * "JavaFX runtime components are missing". Arrancar desde una clase que NO extiende
 * {@code Application} evita ese chequeo.
 */
public class LanzadorServidor {
    public static void main(String[] args) {
        Application.launch(AppServidor.class, args);
    }
}
