package ar.edu.unlu.mayormenor;

import javafx.application.Application;

/**
 * Clase para correr la aplicacion con el boton "Run" de IntelliJ.
 * <p>
 * Si corrés directamente {@link App#main}, Java puede quejarse con
 * "JavaFX runtime components are missing". Arrancar desde una clase que
 * NO extiende {@code Application} evita ese chequeo.
 * <p>
 * Lo mismo pasa con las demos: no las corras con ▶ desde su propia clase,
 * arrancalas desde acá.
 */
public class Lanzador {
    public static void main(String[] args) {
        // El juego:
        Application.launch(App.class, args);

        // Demos de componentes (Parte 1 de la guía): comentá la línea del juego
        // y descomentá la demo que quieras ver. Se lanza una sola por vez.
        // Application.launch(ar.edu.unlu.mayormenor.demos.DemoLayouts.class, args);
        // Application.launch(ar.edu.unlu.mayormenor.demos.DemoMenus.class, args);
        // Application.launch(ar.edu.unlu.mayormenor.demos.DemoEventos.class, args);
        // Application.launch(ar.edu.unlu.mayormenor.demos.DemoDialogos.class, args);
    }
}
