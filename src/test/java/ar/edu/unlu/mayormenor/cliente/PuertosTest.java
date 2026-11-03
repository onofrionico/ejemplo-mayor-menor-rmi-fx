package ar.edu.unlu.mayormenor.cliente;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuertosTest {

    /** Un puerto que el sistema operativo nos confirma que está libre. */
    private static int puertoLibre() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    @Test
    void primerLibreDevuelveElMismoSiEstaLibre() throws IOException {
        int libre = puertoLibre();
        assertEquals(libre, Puertos.primerLibre(libre));
    }

    @Test
    void primerLibreSaltaElOcupado() throws IOException {
        // Como cuando ya hay un cliente abierto en la misma PC usando ese puerto
        try (ServerSocket ocupado = new ServerSocket(0)) {
            int puerto = ocupado.getLocalPort();
            assertTrue(Puertos.primerLibre(puerto) > puerto);
        }
    }
}
