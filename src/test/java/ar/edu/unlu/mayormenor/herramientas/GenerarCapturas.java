package ar.edu.unlu.mayormenor.herramientas;

import ar.edu.unlu.mayormenor.controlador.Controlador;
import ar.edu.unlu.mayormenor.modelo.Apuesta;
import ar.edu.unlu.mayormenor.modelo.Carta;
import ar.edu.unlu.mayormenor.modelo.Mazo;
import ar.edu.unlu.mayormenor.modelo.Partida;
import ar.edu.unlu.mayormenor.vista.VistaGrafica;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static ar.edu.unlu.mayormenor.modelo.Palo.*;

/**
 * Herramienta del equipo docente (no es un test): genera las capturas de pantalla del post.
 * <p>
 * Arma una partida <b>local</b> (sin RMI) con un mazo fijo y tres jugadores, cada uno con su
 * Controlador y su VistaGrafica, la hace avanzar y guarda cada pantalla en {@code target/capturas/}.
 * <pre>
 * ./mvnw -q test-compile exec:java -Dexec.mainClass=ar.edu.unlu.mayormenor.herramientas.GenerarCapturas -Dexec.classpathScope=test
 * </pre>
 */
public class GenerarCapturas {

    private static final File CARPETA = new File("target/capturas");

    public static void main(String[] args) throws Exception {
        CARPETA.mkdirs();
        Platform.startup(() -> {
        });

        Partida partida = new Partida(() -> new Mazo(List.of(
                new Carta(5, ORO), new Carta(7, COPA), new Carta(10, ESPADA), new Carta(12, BASTO), new Carta(3, ORO))));
        Controlador ana = jugador(partida);
        Controlador beto = jugador(partida);
        Controlador caro = jugador(partida);
        Stage[] ventanas = new Stage[3];
        enFx(() -> {
            ventanas[0] = conVista(ana);
            ventanas[1] = conVista(beto);
            ventanas[2] = conVista(caro);
        });

        enFx(() -> {
            ana.entrar("Ana");
            beto.entrar("Beto");
            caro.entrar("Caro");
        });
        capturar(ventanas[0], "sala-admin.png");
        capturar(ventanas[1], "sala-espera.png");

        enFx(ana::empezar);
        capturar(ventanas[0], "mesa-turno-ana.png");
        enFx(() -> ana.apostar(Apuesta.MENOR));   // 7 > 5: Ana queda eliminada, le toca a Beto
        capturar(ventanas[1], "mesa-turno.png");
        capturar(ventanas[2], "mesa-esperando.png");
        capturar(ventanas[0], "mesa-eliminado.png");

        enFx(() -> beto.apostar(Apuesta.MAYOR));  // 10 > 7
        enFx(() -> caro.apostar(Apuesta.MENOR));  // 12 > 10: Caro queda eliminada, gana Beto
        capturar(ventanas[1], "resultado.png");
        capturar(ventanas[2], "resultado-perdio.png");

        Platform.exit();
        System.exit(0);
    }

    private static Controlador jugador(Partida partida) throws RemoteException {
        Controlador controlador = new Controlador();
        controlador.setModeloRemoto(partida);
        partida.agregarObservador(controlador);
        return controlador;
    }

    private static Stage conVista(Controlador controlador) {
        Stage stage = new Stage();
        controlador.setVista(new VistaGrafica(stage, controlador));
        return stage;
    }

    /** Ejecuta en el hilo de JavaFX y espera a que se procesen también los runLater que encoló. */
    private static void enFx(Runnable accion) throws InterruptedException {
        esperar(accion);
        esperar(() -> {
        });
        esperar(() -> {
        });
    }

    private static void esperar(Runnable accion) throws InterruptedException {
        CountDownLatch listo = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                accion.run();
            } finally {
                listo.countDown();
            }
        });
        if (!listo.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX no respondió");
        }
    }

    private static void capturar(Stage ventana, String archivo) throws Exception {
        WritableImage[] imagen = new WritableImage[1];
        esperar(() -> imagen[0] = ventana.getScene().snapshot(null));
        ImageIO.write(SwingFXUtils.fromFXImage(imagen[0], null), "png", new File(CARPETA, archivo));
        System.out.println("Captura: " + archivo);
    }
}
