package ar.edu.unlu.mayormenor.herramientas;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.demos.DemoDialogos;
import ar.edu.unlu.mayormenor.demos.DemoEventos;
import ar.edu.unlu.mayormenor.demos.DemoLayouts;
import ar.edu.unlu.mayormenor.demos.DemoMenus;
import ar.edu.unlu.mayormenor.modelo.Carta;
import ar.edu.unlu.mayormenor.modelo.Mazo;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.vista.VistaGrafica;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.transform.Transform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static ar.edu.unlu.mayormenor.modelo.Palo.*;

/**
 * Herramienta del equipo docente (no es un test): genera las capturas de pantalla
 * que usa el post de la autoguía, a partir del código real del repo.
 * Para las pantallas del juego arma el MVC completo con un mazo fijo y "hace clic"
 * con {@code Button.fire()}, así también sirve como prueba de humo de la interfaz.
 * <p>
 * ./mvnw test-compile exec:java -Dexec.mainClass=ar.edu.unlu.mayormenor.herramientas.GenerarCapturas -Dexec.classpathScope=test
 * <p>
 * Las imágenes quedan en target/capturas/.
 */
public class GenerarCapturas {

    private static final File DESTINO = new File("target/capturas");

    public static void main(String[] args) throws Exception {
        DESTINO.mkdirs();
        CountDownLatch listo = new CountDownLatch(1);
        Platform.startup(() -> {
            try {
                capturar(DemoLayouts.crearRaiz(), 520, 420, "demo-layouts.png");
                capturar(DemoMenus.crearRaiz(), 420, 250, "demo-menus.png");
                capturar(new DemoEventos().crearRaiz(), 380, 280, "demo-eventos.png");
                capturar(DemoDialogos.crearRaiz(), 320, 380, "demo-dialogos.png");
                capturarJuego();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                listo.countDown();
            }
        });
        listo.await();
        Platform.exit();
    }

    private static void capturarJuego() throws Exception {
        Partida modelo = new Partida(() -> new Mazo(List.of(
                new Carta(5, ORO), new Carta(11, ESPADA), new Carta(3, COPA), new Carta(7, BASTO))));
        Controlador controlador = new Controlador(modelo);
        Stage stage = new Stage();
        VistaGrafica vista = new VistaGrafica(stage, controlador);
        controlador.setVista(vista);

        vista.mostrarMenu();
        guardar(stage, "juego-menu.png");

        clic(stage, "#btnJugar");      // menú → mesa con el 5 de oros
        clic(stage, "#btnMayor");      // 11 de espadas: acierto
        guardar(stage, "juego-mesa.png");
        guardarPortada(stage);

        clic(stage, "#btnMayor");      // 3 de copas: error → pantalla de resultado
        guardar(stage, "juego-resultado.png");

        clic(stage, "#btnJugarDeNuevo"); // vuelve a la mesa con puntaje 0
        Label puntaje = (Label) stage.getScene().lookup("#lblPuntaje");
        if (!"Puntaje: 0".equals(puntaje.getText())) {
            throw new IllegalStateException("Jugar de nuevo no reinició el puntaje: " + puntaje.getText());
        }
        // Ayuda > Reglas dos veces: debe quedar UNA sola ventana de reglas, no modal
        menu(stage, "Reglas");
        menu(stage, "Reglas");
        long ventanas = Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).count();
        if (ventanas != 1) {
            throw new IllegalStateException("Se esperaba 1 ventana de reglas y hay " + ventanas);
        }
        // Con las reglas abiertas se puede seguir jugando (no modal)
        clic(stage, "#btnMenor");      // 11 de espadas: error

        // Partida > Nueva desde la mesa: hay que volver a la mesa antes
        clic(stage, "#btnJugarDeNuevo");
        menu(stage, "Nueva");
        // Con las reglas todavía abiertas, Reglas de nuevo no debe abrir otra ventana
        menu(stage, "Reglas");
        long reglasAbiertas = Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).count();
        if (reglasAbiertas != 1) {
            throw new IllegalStateException("Después de Nueva, se esperaba 1 ventana de reglas y hay " + reglasAbiertas);
        }
        Label otra = (Label) stage.getScene().lookup("#lblPuntaje");
        if (!"Puntaje: 0".equals(otra.getText())) {
            throw new IllegalStateException("Partida > Nueva no reinició el puntaje");
        }
        guardar(stage, "juego-mesa-menu.png");

        // Cerrar la ventana principal con la X cierra también las reglas
        menu(stage, "Reglas");
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        stage.close();
        if (Window.getWindows().stream().anyMatch(Window::isShowing)) {
            throw new IllegalStateException("Quedó una ventana abierta al cerrar la principal");
        }
        stage.show();
        vista.mostrarJuego();
        modelo.iniciar();
        clic(stage, "#btnMayor");      // 11 de espadas: acierto
        clic(stage, "#btnMayor");      // 3 de copas: error
        clic(stage, "#btnVolver");     // vuelve al menú
        if (stage.getScene().lookup("#btnJugar") == null) {
            throw new IllegalStateException("Volver al menú no mostró el menú");
        }
        stage.close();
    }

    private static void clic(Stage stage, String selector) {
        Button boton = (Button) stage.getScene().lookup(selector);
        if (boton == null) {
            throw new IllegalStateException("No se encontró " + selector);
        }
        boton.fire();
    }

    /** "Hace clic" en un ítem de la barra de menú buscándolo por su texto. */
    private static void menu(Stage stage, String texto) {
        MenuBar barra = (MenuBar) stage.getScene().lookup(".menu-bar");
        barra.getMenus().stream()
                .flatMap(m -> m.getItems().stream())
                .filter(i -> texto.equals(i.getText()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay ítem de menú " + texto))
                .fire();
    }

    private static void capturar(Parent raiz, double ancho, double alto, String archivo) throws Exception {
        escribir(new Scene(raiz, ancho, alto).snapshot(null), archivo);
    }

    private static void guardar(Stage stage, String archivo) throws Exception {
        escribir(stage.getScene().snapshot(null), archivo);
    }

    /** Portada del post (cover.jpg): la mesa al doble de resolución, en JPG. */
    private static void guardarPortada(Stage stage) throws Exception {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setTransform(Transform.scale(2, 2));
        WritableImage imagen = stage.getScene().getRoot().snapshot(parametros, null);
        BufferedImage conAlfa = SwingFXUtils.fromFXImage(imagen, null);
        BufferedImage rgb = new BufferedImage(conAlfa.getWidth(), conAlfa.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgb.createGraphics().drawImage(conAlfa, 0, 0, null);
        File salida = new File(DESTINO, "cover.jpg");
        ImageIO.write(rgb, "jpg", salida);
        System.out.println("Captura: " + salida.getPath());
    }

    private static void escribir(WritableImage imagen, String archivo) throws Exception {
        File salida = new File(DESTINO, archivo);
        ImageIO.write(SwingFXUtils.fromFXImage(imagen, null), "png", salida);
        System.out.println("Captura: " + salida.getPath());
    }
}
