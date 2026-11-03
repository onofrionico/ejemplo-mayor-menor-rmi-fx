package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Jugador;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

/**
 * Controlador de la vista sala.fxml (la sala de espera).
 * <p>
 * Todos los clientes ven la misma lista de jugadores, que se actualiza sola cuando entra alguien.
 * Solo el admin ve el botón para empezar; los demás ven a quién están esperando.
 */
public class SalaController {

    @FXML
    private ListView<String> lstJugadores;
    @FXML
    private Button btnEmpezar;
    @FXML
    private Label lblEspera;

    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    public void mostrar(List<Jugador> jugadores, Jugador admin, Jugador yo) {
        lstJugadores.getItems().setAll(jugadores.stream()
                .map(j -> j.nombre()
                        + (j.esElMismoQue(admin) ? " (admin)" : "")
                        + (j.esElMismoQue(yo) ? " (vos)" : ""))
                .toList());
        boolean soyAdmin = yo.esElMismoQue(admin);
        // setManaged(false) además de setVisible(false): así el nodo oculto no ocupa lugar
        btnEmpezar.setVisible(soyAdmin);
        btnEmpezar.setManaged(soyAdmin);
        lblEspera.setVisible(!soyAdmin);
        lblEspera.setManaged(!soyAdmin);
        lblEspera.setText("Esperando a que " + admin.nombre() + " empiece la partida…");
    }

    @FXML
    private void onEmpezar() {
        controlador.empezar();
    }
}
