package edu.escuelaing.arep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Punto 2.1 — Servidor mínimo de línea base (parte del ejercicio 4.4 de la guía de redes).
 *
 * Acepta UNA sola conexión, lee UNA petición HTTP, imprime sus partes por consola,
 * devuelve una página HTML pequeña y TERMINA. Sirve para observar el intercambio HTTP:
 *   - Línea de petición: método, ruta y versión del protocolo.
 *   - Respuesta: estado, Content-Type, la línea en blanco que separa cabeceras del cuerpo,
 *     y el HTML devuelto.
 *
 * Al terminar tras una conexión, se comprueba que efectivamente solo atiende una.
 */
public class MinimalHttpServer {

    public static void main(String[] args) throws IOException {
        int port = puertoDesde(args, 35000);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor mínimo escuchando en http://localhost:" + port);
            System.out.println("Esperando UNA conexión...");

            try (Socket client = serverSocket.accept()) {
                System.out.println("\nConexión aceptada desde " + client.getInetAddress());

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

                // ---------- Leer e imprimir la PETICIÓN ----------
                String requestLine = in.readLine();
                System.out.println("Línea de petición : " + requestLine);
                if (requestLine != null) {
                    String[] parts = requestLine.split(" ");
                    if (parts.length >= 3) {
                        System.out.println("   método  : " + parts[0]);
                        System.out.println("   ruta    : " + parts[1]);
                        System.out.println("   versión : " + parts[2]);
                    }
                }
                System.out.println("Cabeceras recibidas:");
                String header;
                while ((header = in.readLine()) != null && !header.isEmpty()) {
                    System.out.println("   " + header);
                }

                // ---------- Enviar la RESPUESTA ----------
                String body = "<!doctype html><html lang=\"es\"><head>"
                        + "<meta charset=\"UTF-8\"><title>Servidor mínimo</title></head>"
                        + "<body><h1>Servidor mínimo AREP</h1>"
                        + "<p>Se atendió una sola conexión. El servidor termina ahora.</p>"
                        + "</body></html>";

                out.print("HTTP/1.1 200 OK\r\n");
                out.print("Content-Type: text/html; charset=UTF-8\r\n");
                out.print("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n");
                out.print("\r\n");              // línea en blanco: separa cabeceras del cuerpo
                out.print(body);
                out.flush();

                System.out.println("\nRespuesta enviada. Cerrando la conexión y terminando el proceso.");
            }
        }
    }

    /** Lee el puerto de un argumento, si no de la variable de entorno PORT, si no usa el por defecto. */
    private static int puertoDesde(String[] args, int porDefecto) {
        if (args.length > 0) return Integer.parseInt(args[0]);
        String env = System.getenv("PORT");
        return env != null ? Integer.parseInt(env) : porDefecto;
    }
}
