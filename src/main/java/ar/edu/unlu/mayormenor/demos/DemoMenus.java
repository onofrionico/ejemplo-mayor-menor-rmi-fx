package ar.edu.unlu.mayormenor.demos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Demo de menús: MenuBar → Menu → MenuItem.
 * <p>
 * En Swing era: JMenuBar → JMenu → JMenuItem, y la barra se ponía con frame.setJMenuBar(...).
 * En JavaFX no hay un lugar especial: la barra es un nodo más, que se ubica en el "top" de un BorderPane.
 *
 * Correr con: ./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoMenus
 */
public class DemoMenus extends Application {

    public static Parent crearRaiz() {
        Label ultimo = new Label("Elegí una opción del menú");

        MenuItem nueva = new MenuItem("Nueva");
        nueva.setAccelerator(KeyCombination.keyCombination("Shortcut+N")); // Ctrl+N (Cmd+N en macOS)
        nueva.setOnAction(e -> ultimo.setText("Elegiste: Partida > Nueva"));

        MenuItem salir = new MenuItem("Salir");
        salir.setOnAction(e -> Platform.exit());

        Menu partida = new Menu("Partida");
        partida.getItems().addAll(nueva, new SeparatorMenuItem(), salir);

        MenuItem reglas = new MenuItem("Reglas");
        reglas.setOnAction(e -> ultimo.setText("Elegiste: Ayuda > Reglas"));
        Menu ayuda = new Menu("Ayuda");
        ayuda.getItems().add(reglas);

        MenuBar barra = new MenuBar(partida, ayuda);

        BorderPane raiz = new BorderPane();
        raiz.setTop(barra);
        raiz.setCenter(ultimo);
        return raiz;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Demo: menús");
        stage.setScene(new Scene(crearRaiz(), 420, 250));
        stage.show();
    }
}
