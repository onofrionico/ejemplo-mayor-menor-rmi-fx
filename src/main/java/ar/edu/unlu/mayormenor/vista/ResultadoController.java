package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Controlador de la vista resultado.fxml (fin de partida). */
public class ResultadoController {

    @FXML
    private Label lblMensaje;
    @FXML
    private Label lblPuntaje;

    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    public void mostrar(int puntaje, boolean ganada) {
        lblMensaje.setText(ganada ? "¡Ganaste! Te quedaste sin cartas." : "Perdiste.");
        lblPuntaje.setText("Puntaje: " + puntaje);
    }

    @FXML
    private void onJugarDeNuevo() {
        controlador.jugar();
    }

    @FXML
    private void onVolver() {
        controlador.volverAlMenu();
    }
}
