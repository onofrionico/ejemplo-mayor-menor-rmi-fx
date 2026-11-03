package ar.edu.unlu.mayormenor.demos;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Demo de manejo de eventos: cuatro formas de responder al clic de un botón.
 * <p>
 * En Swing era: ActionListener con actionPerformed(ActionEvent).
 * En JavaFX es: EventHandler&lt;ActionEvent&gt; con handle(ActionEvent), registrado con setOnAction(...).
 *
 * Correr con: ./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoEventos
 */
public class DemoEventos extends Application {

    private final Label respuesta = new Label("Todavía no hiciste clic");
    private final Button btnSumar = new Button("1a. Receptor centralizado: Sumar");
    private final Button btnRestar = new Button("1b. Receptor centralizado: Restar");
    private final Button btnInterna = new Button("2. Clase interna");
    private final Button btnAnonima = new Button("3. Clase anónima");
    private final Button btnLambda = new Button("4. Lambda");

    /**
     * 1. Receptor centralizado: UN objeto recibe los eventos de VARIOS botones
     * y pregunta quién los generó. Con muchos botones, este if crece sin fin.
     */
    private class ReceptorCentralizado implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            if (e.getSource() == btnSumar) {
                respuesta.setText("Respondió el receptor centralizado (Sumar)");
            } else if (e.getSource() == btnRestar) {
                respuesta.setText("Respondió el receptor centralizado (Restar)");
            }
        }
    }

    /** 2. Clase interna con nombre: un receptor por botón, sin preguntar por la fuente. */
    private class ReceptorInterno implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            respuesta.setText("Respondió la clase interna");
        }
    }

    public VBox crearRaiz() {
        ReceptorCentralizado centralizado = new ReceptorCentralizado();
        btnSumar.setOnAction(centralizado);
        btnRestar.setOnAction(centralizado);

        btnInterna.setOnAction(new ReceptorInterno());

        // 3. Clase anónima: se define y se instancia en el mismo lugar
        btnAnonima.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                respuesta.setText("Respondió la clase anónima");
            }
        });

        // 4. Lambda: lo mismo que la anónima, escrito en una línea (EventHandler tiene un solo método)
        btnLambda.setOnAction(e -> respuesta.setText("Respondió la lambda"));

        VBox raiz = new VBox(10, btnSumar, btnRestar, btnInterna, btnAnonima, btnLambda, respuesta);
        raiz.setPadding(new Insets(15));
        return raiz;
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(crearRaiz(), 380, 280);

        // Eventos de teclado: ENTER dispara el botón de la lambda
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                btnLambda.fire();
            }
        });

        // Evento de ventana: en Swing era WindowAdapter.windowClosing(...)
        stage.setOnCloseRequest(e -> System.out.println("Se está cerrando la ventana"));

        stage.setTitle("Demo: manejo de eventos");
        stage.setScene(scene);
        stage.show();
    }
}
