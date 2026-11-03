package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Carta;
import ar.edu.unlu.mayormenor.modelo.Jugador;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * La vista gráfica: implementa {@link IVista} con JavaFX.
 * <p>
 * Coordina las pantallas (cada una es un FXML con su controlador de vista)
 * sobre una única ventana ({@link Stage}).
 * <p>
 * <b>Por qué {@link Platform#runLater}.</b> JavaFX tiene un hilo propio, y es el único que puede
 * tocar la ventana. Los avisos del servidor no empiezan con un clic en esta ventana: la librería los
 * recibe en otro hilo y desde ahí el Controlador llama a esta vista. Si la vista tocara la pantalla
 * directamente, JavaFX tiraría {@code IllegalStateException: Not on FX application thread}. Por eso
 * cada método de {@link IVista} le encarga su trabajo a JavaFX con {@code Platform.runLater}
 * ("cuando puedas, hacé esto") y termina enseguida. Nunca hay que hacer algo que se quede
 * esperando (como un diálogo con {@code showAndWait()}) fuera del {@code runLater}: el servidor
 * espera a que cada cliente termine de recibir el aviso.
 */
public class VistaGrafica implements IVista {

    private final Stage stage;
    private final Controlador controlador;
    private JuegoController juego;

    public VistaGrafica(Stage stage, Controlador controlador) {
        this.stage = stage;
        this.controlador = controlador;
        stage.setTitle("Mayor o Menor");
        // Al cerrar la ventana principal, cerramos también la de reglas (si quedó abierta)
        stage.setOnCloseRequest(e -> {
            if (juego != null) {
                juego.cerrarReglas();
            }
        });
    }

    @Override
    public void mostrarSala(List<Jugador> jugadores, Jugador admin, Jugador yo) {
        Platform.runLater(() -> {
            stage.setTitle("Mayor o Menor — " + yo.nombre());
            SalaController sala = cargar("/fxml/sala.fxml");
            sala.setControlador(controlador);
            sala.mostrar(jugadores, admin, yo);
        });
    }

    @Override
    public void mostrarJuego() {
        Platform.runLater(() -> {
            // Si había una mesa anterior con las reglas abiertas, las cerramos: la mesa nueva tiene su propio controlador
            if (juego != null) {
                juego.cerrarReglas();
            }
            juego = cargar("/fxml/juego.fxml");
            juego.setControlador(controlador);
        });
    }

    @Override
    public void mostrarMesa(Carta carta, int restantes, List<Jugador> jugadores, Jugador deTurno, Jugador yo) {
        // Misma escena: solo cambian la carta, los textos y qué botones se pueden usar
        Platform.runLater(() -> juego.mostrarMesa(carta, restantes, jugadores, deTurno, yo));
    }

    @Override
    public void mostrarResultado(List<Jugador> ganadores, List<Jugador> jugadores, Jugador yo, boolean soyAdmin) {
        Platform.runLater(() -> {
            // La partida terminó: que no se pueda seguir apostando en la mesa
            if (juego != null) {
                juego.deshabilitarApuestas();
            }
            // Cambio de escena: de la mesa a la pantalla de resultado
            ResultadoController resultado = cargar("/fxml/resultado.fxml");
            resultado.setControlador(controlador);
            resultado.mostrar(ganadores, jugadores, yo, soyAdmin);
        });
    }

    @Override
    public void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
            // Antes de entrar a la sala la ventana todavía no tiene escena, y JavaFX no deja usarla como dueña
            if (stage.getScene() != null) {
                alerta.initOwner(stage);
            }
            alerta.setHeaderText("Algo salió mal");
            alerta.showAndWait();
        });
    }

    /**
     * Carga un FXML, lo pone como escena de la ventana y devuelve su controlador de vista.
     * <p>
     * Usamos una instancia de FXMLLoader (y no el atajo estático FXMLLoader.load) porque
     * necesitamos {@code loader.getController()} para pasarle el Controlador MVC.
     */
    private <T> T cargar(String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent raiz = loader.load();
            // Opción A (la que usamos): una Scene nueva por pantalla.
            //   La ventana toma el tamaño preferido de cada FXML.
            stage.setScene(new Scene(raiz));
            // Opción B: reutilizar la misma Scene y cambiar solo su raíz:
            //   if (stage.getScene() == null) stage.setScene(new Scene(raiz));
            //   else stage.getScene().setRoot(raiz);
            //   Conserva el tamaño de la ventana y las hojas de estilo de la Scene.
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar " + rutaFxml, e);
        }
    }
}
