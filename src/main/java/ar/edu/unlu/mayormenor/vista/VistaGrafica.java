package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Carta;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * La vista gráfica: implementa {@link IVista} con JavaFX.
 * <p>
 * Coordina las pantallas (cada una es un FXML con su controlador de vista)
 * sobre una única ventana ({@link Stage}).
 * <p>
 * Nota: las notificaciones del modelo llegan en el mismo hilo de JavaFX (todo pasa por un clic),
 * por eso no hace falta {@code Platform.runLater}. Sí haría falta si el modelo notificara desde
 * otro hilo, como el chat con RMI.
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
    public void mostrarMenu() {
        MenuController menu = cargar("/fxml/menu.fxml");
        menu.setControlador(controlador);
    }

    @Override
    public void mostrarJuego() {
        // Si había una mesa anterior con las reglas abiertas, las cerramos: la mesa nueva tiene su propio controlador
        if (juego != null) {
            juego.cerrarReglas();
        }
        juego = cargar("/fxml/juego.fxml");
        juego.setControlador(controlador);
    }

    @Override
    public void mostrarCarta(Carta carta, int puntaje, int restantes) {
        // Misma escena: solo cambian la carta y los textos
        juego.mostrarCarta(carta, puntaje, restantes);
    }

    @Override
    public void mostrarResultado(int puntaje, boolean ganada) {
        // La partida terminó: que no se pueda seguir apostando en la mesa
        if (juego != null) {
            juego.deshabilitarApuestas();
        }
        // Cambio de escena: de la mesa a la pantalla de resultado
        ResultadoController resultado = cargar("/fxml/resultado.fxml");
        resultado.setControlador(controlador);
        resultado.mostrar(puntaje, ganada);
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
