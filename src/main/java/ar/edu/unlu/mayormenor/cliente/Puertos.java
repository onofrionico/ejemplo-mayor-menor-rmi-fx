package ar.edu.unlu.mayormenor.cliente;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Ayuda para elegir el puerto del cliente.
 * <p>
 * Cada cliente RMI también "escucha" en un puerto, porque el servidor le tiene que avisar
 * (llamar a {@code actualizar}). Si abrís dos clientes en la misma PC, no pueden usar el mismo
 * puerto: el segundo fallaría con {@code ExportException: Port already in use}.
 */
public final class Puertos {

    private Puertos() {
    }

    /** El primer puerto, a partir de {@code desde}, que no está usando nadie en esta PC. */
    public static int primerLibre(int desde) {
        for (int puerto = desde; puerto <= 65535; puerto++) {
            try (ServerSocket prueba = new ServerSocket(puerto)) {
                return puerto;
            } catch (IOException ocupado) {
                // Probamos con el siguiente
            }
        }
        throw new IllegalStateException("No hay puertos libres desde el " + desde);
    }
}
