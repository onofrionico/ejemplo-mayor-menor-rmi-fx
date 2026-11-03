package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Jugador;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.List;
import java.util.stream.Collectors;

/** Controlador de la vista resultado.fxml (fin de partida): la ven todos los jugadores a la vez. */
public class ResultadoController {

    @FXML
    private Label lblMensaje;
    @FXML
    private Label lblPuntaje;
    @FXML
    private Button btnJugarDeNuevo;

    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    public void mostrar(List<Jugador> ganadores, List<Jugador> jugadores, Jugador yo, boolean soyAdmin) {
        lblMensaje.setText(mensaje(ganadores, jugadores, yo));
        lblPuntaje.setText("Aciertos: " + jugadores.stream()
                .map(j -> j.nombre() + " " + j.aciertos())
                .collect(Collectors.joining(" · ")));
        // Solo el admin puede empezar otra partida
        btnJugarDeNuevo.setVisible(soyAdmin);
        btnJugarDeNuevo.setManaged(soyAdmin);
    }

    private static String mensaje(List<Jugador> ganadores, List<Jugador> jugadores, Jugador yo) {
        boolean gane = ganadores.stream().anyMatch(yo::esElMismoQue);
        if (jugadores.size() == 1) {
            // Jugando de a uno, como en la Clase 11
            return gane ? "¡Ganaste! Te quedaste sin cartas." : "Perdiste.";
        }
        if (ganadores.isEmpty()) {
            return "Nadie ganó.";
        }
        if (ganadores.size() == 1) {
            return gane ? "¡Ganaste!" : "Ganó " + ganadores.get(0).nombre() + ".";
        }
        String nombres = ganadores.stream().map(Jugador::nombre).collect(Collectors.joining(", "));
        return (gane ? "¡Empate! Ganaron " : "Empate: ganaron ") + nombres + ".";
    }

    @FXML
    private void onJugarDeNuevo() {
        controlador.empezar();
    }

    @FXML
    private void onVolver() {
        controlador.volverALaSala();
    }
}
