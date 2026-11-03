package ar.edu.unlu.mayormenor;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Mazo;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.vista.VistaGrafica;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Random;

/**
 * Punto de entrada de la aplicacion JavaFX: arma el MVC y muestra el menú.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Partida modelo = new Partida(() -> Mazo.mezclado(new Random()));
        Controlador controlador = new Controlador(modelo);
        VistaGrafica vista = new VistaGrafica(stage, controlador);
        controlador.setVista(vista);
        vista.mostrarMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
