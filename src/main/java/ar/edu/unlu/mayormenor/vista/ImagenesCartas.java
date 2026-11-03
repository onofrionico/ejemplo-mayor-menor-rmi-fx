package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.modelo.Carta;
import javafx.scene.image.Image;

import java.net.URL;

/**
 * Sabe qué archivo de imagen corresponde a cada carta.
 * <p>
 * Esto es responsabilidad de la <b>vista</b>: el modelo ({@link Carta}) no sabe que existen imágenes.
 * Las imágenes están en src/main/resources/img/cartas/ con nombres como "espada-1.png".
 */
public final class ImagenesCartas {

    public static final String RUTA_REVERSO = "/img/cartas/reverso.png";

    private ImagenesCartas() {
    }

    public static String ruta(Carta carta) {
        return "/img/cartas/" + carta.palo().name().toLowerCase() + "-" + carta.numero() + ".png";
    }

    public static Image imagen(Carta carta) {
        return cargar(ruta(carta));
    }

    public static Image reverso() {
        return cargar(RUTA_REVERSO);
    }

    private static Image cargar(String ruta) {
        URL url = ImagenesCartas.class.getResource(ruta);
        if (url == null) {
            throw new IllegalStateException("No se encontró la imagen " + ruta);
        }
        return new Image(url.toExternalForm());
    }
}
