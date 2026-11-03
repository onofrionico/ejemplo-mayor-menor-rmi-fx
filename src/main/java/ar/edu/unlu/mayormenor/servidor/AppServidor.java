package ar.edu.unlu.mayormenor.servidor;

import ar.edu.unlu.mayormenor.modelo.Mazo;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.servidor.Servidor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * El servidor: crea la única {@link Partida} y la publica con RMI para que se conecten los clientes.
 * <p>
 * {@code Servidor.iniciar(modelo)} (de la librería) crea el <i>registry</i> en la IP y el puerto
 * elegidos y registra ahí al modelo. Desde ese momento los clientes pueden pedirle el stub.
 * La ventana solo muestra que el servidor está corriendo: si la cerrás, se apaga el servidor.
 */
public class AppServidor extends Application {

    @Override
    public void start(Stage stage) {
        List<String> ips = Util.getIpDisponibles();
        ChoiceDialog<String> dialogoIp = new ChoiceDialog<>(ips.get(0), ips);
        dialogoIp.setTitle("Servidor");
        dialogoIp.setHeaderText("¿En qué IP escucha el servidor?");
        dialogoIp.setContentText("IP:");
        Optional<String> ip = dialogoIp.showAndWait();
        if (ip.isEmpty()) {
            Platform.exit();
            return;
        }

        TextInputDialog dialogoPuerto = new TextInputDialog("8888");
        dialogoPuerto.setTitle("Servidor");
        dialogoPuerto.setHeaderText("¿En qué puerto escucha el servidor?");
        dialogoPuerto.setContentText("Puerto:");
        Optional<String> puerto = dialogoPuerto.showAndWait();
        if (puerto.isEmpty()) {
            Platform.exit();
            return;
        }

        Partida modelo = new Partida(() -> Mazo.mezclado(new Random()));
        try {
            new Servidor(ip.get(), Integer.parseInt(puerto.get().trim())).iniciar(modelo);
        } catch (RemoteException | RMIMVCException | NumberFormatException e) {
            Alert error = new Alert(Alert.AlertType.ERROR, e.getMessage());
            error.setHeaderText("No se pudo iniciar el servidor");
            error.showAndWait();
            Platform.exit();
            return;
        }

        Label estado = new Label("Servidor corriendo");
        estado.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        Label datos = new Label("IP: " + ip.get() + "     Puerto: " + puerto.get().trim());
        VBox raiz = new VBox(12, estado, datos);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(24));
        stage.setTitle("Mayor o Menor — Servidor");
        stage.setScene(new Scene(raiz, 320, 130));
        // Al cerrar la ventana se apaga el servidor (System.exit corta también los hilos de RMI)
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }
}
