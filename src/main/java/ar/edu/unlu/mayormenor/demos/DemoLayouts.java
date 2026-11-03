package ar.edu.unlu.mayormenor.demos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Demo de layouts: una "mesa de juego" armada anidando contenedores.
 * <p>
 * Cada layout administra un espacio por el cual compiten los nodos que contiene.
 * Combinando layouts simples se arman interfaces sofisticadas:
 * <pre>
 *  BorderPane
 *   ├─ top    → Label (título)
 *   ├─ center → StackPane (mazo: cartas encimadas)
 *   ├─ right  → VBox (puntaje y turno, uno debajo del otro)
 *   └─ bottom → HBox (la mano del jugador, una al lado de la otra)
 * </pre>
 * En Swing era: BorderLayout + BoxLayout + FlowLayout dentro de JPanels.
 *
 * Correr con: ./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoLayouts
 */
public class DemoLayouts extends Application {

    public static Parent crearRaiz() {
        // top: el título
        Label titulo = new Label("Mesa de juego");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        BorderPane.setAlignment(titulo, Pos.CENTER);

        // center: StackPane apila sus hijos uno encima del otro (como un mazo)
        StackPane mazo = new StackPane();
        for (int i = 0; i < 3; i++) {
            Rectangle carta = carta(Color.DARKRED);
            carta.setTranslateX(i * 4);
            carta.setTranslateY(i * -4);
            mazo.getChildren().add(carta);
        }

        // right: VBox apila verticalmente (equivale a BoxLayout en el eje Y)
        VBox info = new VBox(10, new Label("Puntaje: 0"), new Label("Turno: Jugador 1"));
        info.setPadding(new Insets(10));

        // bottom: HBox apila horizontalmente (equivale a FlowLayout / BoxLayout en el eje X)
        HBox mano = new HBox(10);
        mano.setAlignment(Pos.CENTER);
        mano.setPadding(new Insets(10));
        for (String nombre : new String[]{"1 de espadas", "7 de oros", "12 de copas"}) {
            Label etiqueta = new Label(nombre);
            mano.getChildren().add(new StackPane(carta(Color.BEIGE), etiqueta));
        }

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(10));
        raiz.setTop(titulo);
        raiz.setCenter(mazo);
        raiz.setRight(info);
        raiz.setBottom(mano);
        return raiz;
    }

    private static Rectangle carta(Color color) {
        Rectangle r = new Rectangle(80, 120, color);
        r.setArcWidth(10);
        r.setArcHeight(10);
        r.setStroke(Color.BLACK);
        return r;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Demo: layouts y anidación");
        stage.setScene(new Scene(crearRaiz(), 520, 420));
        stage.show();
    }
}
