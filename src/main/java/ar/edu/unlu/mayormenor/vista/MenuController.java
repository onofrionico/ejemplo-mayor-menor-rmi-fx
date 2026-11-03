package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controlador de la vista menu.fxml.
 * <p>
 * JavaFX crea esta clase al cargar el FXML, inyecta los campos {@code @FXML}
 * cuyo nombre coincide con un {@code fx:id} y conecta los {@code onAction="#metodo"}.
 * Solo reenvía lo que hace el usuario al Controlador MVC.
 */
public class MenuController {

    @FXML
    private Button btnJugar;

    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    @FXML
    private void onJugar() {
        controlador.jugar();
    }
}
