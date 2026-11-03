package ar.edu.unlu.mayormenor.cliente;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.vista.VistaGrafica;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.cliente.Cliente;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

/**
 * Un cliente: un jugador con su propia ventana.
 * <p>
 * Primero pregunta los datos de red y después se conecta al servidor con
 * {@code Cliente.iniciar(controlador)} (de la librería), que:
 * <ol>
 *   <li>busca el modelo en el registry del servidor y obtiene su stub,</li>
 *   <li>publica al Controlador para que el servidor lo pueda llamar de vuelta,</li>
 *   <li>lo registra como observador del modelo y le entrega el stub con {@code setModeloRemoto}.</li>
 * </ol>
 * Después pide el nombre del jugador y entra a la sala de espera.
 */
public class AppCliente extends Application {

    @Override
    public void start(Stage stage) {
        List<String> ips = Util.getIpDisponibles();
        Optional<String> ipCliente = elegir("¿En qué IP escucha este cliente?", ips);
        if (ipCliente.isEmpty()) {
            return;
        }
        // Si ya hay otro cliente abierto en esta PC, proponemos el siguiente puerto libre
        Optional<String> puertoCliente = preguntar("¿En qué puerto escucha este cliente?",
                String.valueOf(Puertos.primerLibre(9999)));
        if (puertoCliente.isEmpty()) {
            return;
        }
        Optional<String> ipServidor = preguntar("¿En qué IP está el servidor?", "127.0.0.1");
        if (ipServidor.isEmpty()) {
            return;
        }
        Optional<String> puertoServidor = preguntar("¿En qué puerto está el servidor?", "8888");
        if (puertoServidor.isEmpty()) {
            return;
        }

        Controlador controlador = new Controlador();
        VistaGrafica vista = new VistaGrafica(stage, controlador);
        controlador.setVista(vista);
        try {
            Cliente cliente = new Cliente(ipCliente.get(), Integer.parseInt(puertoCliente.get().trim()),
                    ipServidor.get().trim(), Integer.parseInt(puertoServidor.get().trim()));
            cliente.iniciar(controlador);
        } catch (RemoteException | RMIMVCException | NumberFormatException e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "Verificá que el servidor esté corriendo en " + ipServidor.get().trim() + ":"
                            + puertoServidor.get().trim() + "\n\n" + e.getMessage());
            error.setHeaderText("No se pudo conectar al servidor");
            error.showAndWait();
            System.exit(1);
        }
        // Ya conectados: entramos a la sala con un nombre (si el servidor lo rechaza, lo volvemos a pedir)
        boolean adentro = false;
        while (!adentro) {
            Optional<String> nombre = preguntar("¿Cómo te llamás?", "");
            if (nombre.isEmpty()) {
                // Ya estamos registrados como observadores: hay que salir bien, o el servidor nos seguiría avisando
                controlador.salir();
                return;
            }
            adentro = controlador.entrar(nombre.get());
        }
    }

    private Optional<String> elegir(String pregunta, List<String> opciones) {
        ChoiceDialog<String> dialogo = new ChoiceDialog<>(opciones.get(0), opciones);
        dialogo.setTitle("Conexión");
        dialogo.setHeaderText(pregunta);
        return salirSiCancela(dialogo.showAndWait());
    }

    private Optional<String> preguntar(String pregunta, String porDefecto) {
        TextInputDialog dialogo = new TextInputDialog(porDefecto);
        dialogo.setTitle("Conexión");
        dialogo.setHeaderText(pregunta);
        return salirSiCancela(dialogo.showAndWait());
    }

    private static Optional<String> salirSiCancela(Optional<String> respuesta) {
        if (respuesta.isEmpty()) {
            Platform.exit();
        }
        return respuesta;
    }
}
