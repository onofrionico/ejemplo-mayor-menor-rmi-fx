package ar.edu.unlu.mayormenor.demos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Demo de diálogos.
 * <p>
 * En Swing era: JOptionPane.showMessageDialog / showConfirmDialog / showInputDialog, y JDialog.
 * En JavaFX es: Alert, TextInputDialog, ChoiceDialog, y un Stage propio con una modalidad.
 * <ul>
 *   <li><b>Modal</b> ({@code showAndWait()}): bloquea la ventana dueña hasta que se cierra el diálogo.</li>
 *   <li><b>No modal</b> ({@code show()}): se puede seguir usando la ventana principal.</li>
 * </ul>
 *
 * Correr con: ./mvnw javafx:run -Djavafx.mainClass=ar.edu.unlu.mayormenor.demos.DemoDialogos
 */
public class DemoDialogos extends Application {

    public static Parent crearRaiz() {
        Label resultado = new Label(" ");

        Button info = new Button("Alert INFORMATION");
        info.setOnAction(e -> alerta(Alert.AlertType.INFORMATION, "Tu turno", "Te toca jugar."));

        Button warning = new Button("Alert WARNING");
        warning.setOnAction(e -> alerta(Alert.AlertType.WARNING, "Cuidado", "Quedan pocas cartas en el mazo."));

        Button error = new Button("Alert ERROR");
        error.setOnAction(e -> alerta(Alert.AlertType.ERROR, "Jugada inválida", "No podés ligar esa carta."));

        Button confirmacion = new Button("Alert CONFIRMATION");
        confirmacion.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Abandonar la partida?");
            Optional<ButtonType> respuesta = alert.showAndWait(); // modal: espera la respuesta
            boolean acepto = respuesta.isPresent() && respuesta.get() == ButtonType.OK;
            resultado.setText(acepto ? "Elegiste OK" : "Cancelaste");
        });

        Button texto = new Button("TextInputDialog");
        texto.setOnAction(e -> {
            TextInputDialog dialogo = new TextInputDialog();
            dialogo.setHeaderText("¿Cómo te llamás?");
            dialogo.showAndWait().ifPresent(nombre -> resultado.setText("Hola, " + nombre));
        });

        Button eleccion = new Button("ChoiceDialog");
        eleccion.setOnAction(e -> {
            ChoiceDialog<Integer> dialogo = new ChoiceDialog<>(2, 2, 3, 4);
            dialogo.setHeaderText("¿Cuántos jugadores?");
            dialogo.showAndWait().ifPresent(n -> resultado.setText("Jugadores: " + n));
        });

        Button modal = new Button("Ventana modal");
        modal.setOnAction(e -> {
            Stage ventana = ventanaSecundaria(modal.getScene().getWindow(), "Ventana modal",
                    "Mientras esté abierta no podés usar la ventana principal.");
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.showAndWait();
        });

        Button noModal = new Button("Ventana no modal");
        noModal.setOnAction(e -> {
            Stage ventana = ventanaSecundaria(noModal.getScene().getWindow(), "Ventana no modal",
                    "Podés seguir usando la ventana principal.");
            ventana.initModality(Modality.NONE);
            ventana.show();
        });

        VBox raiz = new VBox(8, info, warning, error, confirmacion, texto, eleccion, modal, noModal, resultado);
        raiz.setPadding(new Insets(15));
        return raiz;
    }

    private static void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje);
        alert.setHeaderText(titulo);
        alert.showAndWait();
    }

    private static Stage ventanaSecundaria(Window duenia, String titulo, String mensaje) {
        Stage ventana = new Stage();
        ventana.initOwner(duenia); // queda asociada (y encima) de la ventana principal
        ventana.setTitle(titulo);
        StackPane contenido = new StackPane(new Label(mensaje));
        contenido.setPadding(new Insets(20));
        ventana.setScene(new Scene(contenido));
        return ventana;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Demo: diálogos");
        stage.setScene(new Scene(crearRaiz(), 320, 380));
        stage.show();
    }
}
