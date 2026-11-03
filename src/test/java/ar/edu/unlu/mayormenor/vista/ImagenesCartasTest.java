package ar.edu.unlu.mayormenor.vista;

import ar.edu.unlu.mayormenor.modelo.Carta;
import ar.edu.unlu.mayormenor.modelo.Mazo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ar.edu.unlu.mayormenor.modelo.Palo.ESPADA;
import static org.junit.jupiter.api.Assertions.*;

class ImagenesCartasTest {

    @Test
    void rutaDeUnaCarta() {
        assertEquals("/img/cartas/espada-1.png", ImagenesCartas.ruta(new Carta(1, ESPADA)));
    }

    /**
     * Compara los nombres EXACTOS de los archivos (con mayúsculas y minúsculas):
     * en Windows "Espada-1.png" se encontraría igual, pero en Linux/macOS o dentro de un JAR no.
     */
    @Test
    void existenLasImagenesDeLas40CartasYElReverso() throws IOException, URISyntaxException {
        Path carpeta = Path.of(ImagenesCartas.class.getResource("/img/cartas").toURI());
        Set<String> archivos;
        try (Stream<Path> lista = Files.list(carpeta)) {
            archivos = lista.map(p -> "/img/cartas/" + p.getFileName()).collect(Collectors.toSet());
        }
        Mazo mazo = Mazo.mezclado(new Random(0));
        while (mazo.quedanCartas()) {
            String ruta = ImagenesCartas.ruta(mazo.sacar());
            assertTrue(archivos.contains(ruta), "Falta la imagen " + ruta);
        }
        assertTrue(archivos.contains(ImagenesCartas.RUTA_REVERSO), "Falta la imagen " + ImagenesCartas.RUTA_REVERSO);
    }
}
