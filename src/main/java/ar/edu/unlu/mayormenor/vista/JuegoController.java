package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Apuesta;
import ar.edu.unlu.mayormenor.modelo.Carta;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Controlador de la vista juego.fxml (la mesa).
 * <p>
 * Los botones con imagen están armados en el FXML. En código sería:
 * <pre>
 * ImageView icono = new ImageView(new Image(getClass().getResource("/img/iconos/mayor.png").toExternalForm()));
 * icono.setFitWidth(24);
 * icono.setPreserveRatio(true);
 * Button btnMayor = new Button("Mayor", icono);   // texto + "graphic"
 * </pre>
 */
public class JuegoController {

    @FXML
    private ImageView imgCarta;
    @FXML
    private Label lblPuntaje;
    @FXML
    private Label lblRestantes;
    @FXML
    private Button btnMayor;
    @FXML
    private Button btnMenor;

    private Controlador controlador;
    private Stage ventanaReglas;

    /** JavaFX llama a este método después de inyectar los campos @FXML. */
    @FXML
    private void initialize() {
        imgCarta.setImage(ImagenesCartas.reverso());
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    /** Lo llama la VistaGrafica cuando el modelo avisó que hay una carta nueva. */
    public void mostrarCarta(Carta carta, int puntaje, int restantes) {
        imgCarta.setImage(ImagenesCartas.imagen(carta));
        lblPuntaje.setText("Puntaje: " + puntaje);
        lblRestantes.setText("Quedan: " + restantes);
        btnMayor.setDisable(false);
        btnMenor.setDisable(false);
    }

    /** Cuando la partida termina ya no se puede apostar. */
    public void deshabilitarApuestas() {
        btnMayor.setDisable(true);
        btnMenor.setDisable(true);
    }

    // Los botones NO tocan la vista: solo le avisan al Controlador.
    // La carta cambia cuando el modelo notifica (Observer).

    @FXML
    private void onMayor() {
        controlador.apostar(Apuesta.MAYOR);
    }

    @FXML
    private void onMenor() {
        controlador.apostar(Apuesta.MENOR);
    }

    // ----- Menú -----

    @FXML
    private void onNueva() {
        controlador.jugar();
    }

    /** Diálogo MODAL: showAndWait() frena hasta que el usuario responde. */
    @FXML
    private void onAbandonar() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Abandonar la partida?");
        confirmacion.initOwner(ventana());
        confirmacion.setHeaderText(null);
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            controlador.abandonar();
        }
    }

    /** Ventana NO MODAL: show() vuelve enseguida y se puede seguir jugando con las reglas abiertas. */
    @FXML
    private void onReglas() {
        if (ventanaReglas != null && ventanaReglas.isShowing()) {
            ventanaReglas.toFront();
            return;
        }
        TextArea texto = new TextArea(leerReglas());
        texto.setEditable(false);
        texto.setWrapText(true);
        ventanaReglas = new Stage();
        ventanaReglas.initOwner(ventana());
        ventanaReglas.initModality(Modality.NONE);
        ventanaReglas.setTitle("Reglas");
        ventanaReglas.setScene(new Scene(texto, 380, 300));
        ventanaReglas.show();
    }

    /** La VistaGrafica lo llama al cerrar la ventana principal. */
    public void cerrarReglas() {
        if (ventanaReglas != null) {
            ventanaReglas.close();
        }
    }

    private Window ventana() {
        return imgCarta.getScene().getWindow();
    }

    private String leerReglas() {
        try (InputStream in = getClass().getResourceAsStream("/reglas.txt")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            return "No se encontraron las reglas.";
        }
    }
}
