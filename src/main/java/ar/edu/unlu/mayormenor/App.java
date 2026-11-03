package ar.edu.unlu.mayormenor;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Mazo;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.vista.VistaGrafica;
import javafx.application.Application;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.Random;

/**
 * Punto de entrada de la aplicacion JavaFX: arma el MVC y muestra el menú.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws RemoteException {
        Partida modelo = new Partida(() -> Mazo.mezclado(new Random()));
        // Por ahora todo sigue en la misma JVM: conectamos a mano lo que después hará la librería RMI
        Controlador controlador = new Controlador();
        controlador.setModeloRemoto(modelo);
        modelo.agregarObservador(controlador);
        VistaGrafica vista = new VistaGrafica(stage, controlador);
        controlador.setVista(vista);
        vista.mostrarMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
