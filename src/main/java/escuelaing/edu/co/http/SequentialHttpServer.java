package escuelaing.edu.co.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor secuencial: recursos estáticos + servicios "quemados".
 *   3 — sirve archivos estáticos con su Content-Type.
 *   4 — servicios /app/hello, /app/square, /app/time, /app/health (JSON).
 *   6 — servicio lento /app/slow para observar que el servidor es secuencial.
 *   7 — carga los recursos del classpath (o del disco en desarrollo), para que
 *       la app funcione empaquetada en un .jar desplegable en EC2.
 */
public class SequentialHttpServer {

    private static final Path WEB_ROOT =
            Path.of("src/main/resources/public").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        int port = puertoDesde(args, 35000);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor secuencial en http://localhost:" + port + "  (Ctrl+C para detener)");
            int contador = 0;
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    contador++;
                    atender(client, contador);
                } catch (IOException e) {
                    System.err.println("Error atendiendo la conexión: " + e.getMessage());
                }
            }
        }
    }

    private static void atender(Socket client, int numero) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            OutputStream out = new BufferedOutputStream(client.getOutputStream());

            String requestLine = in.readLine();
            System.out.println("[#" + numero + "] " + requestLine);

            if (requestLine == null || requestLine.isBlank()) {
                responder(out, 400, "Bad Request", "text/html; charset=UTF-8",
                        errorHtml("400 Bad Request", "Petición vacía."));
                return;
            }
            String[] parts = requestLine.split(" ");
            if (parts.length != 3 || !parts[2].startsWith("HTTP/")) {
                responder(out, 400, "Bad Request", "text/html; charset=UTF-8",
                        errorHtml("400 Bad Request", "Línea de petición inválida."));
                return;
            }
            String method = parts[0];
            String rawPath = parts[1];

            String header;
            while ((header = in.readLine()) != null && !header.isEmpty()) { /* ignoradas */ }

            if (!method.equals("GET")) {
                responder(out, 405, "Method Not Allowed", "text/html; charset=UTF-8",
                        errorHtml("405 Method Not Allowed", "Método no soportado: " + escape(method)));
                return;
            }

            String pathOnly = rawPath;
            String query = "";
            int q = rawPath.indexOf('?');
            if (q >= 0) {
                pathOnly = rawPath.substring(0, q);
                query = rawPath.substring(q + 1);
            }

            if (pathOnly.equals("/app/hello")) {
                escribir(out, AppServices.hello(AppServices.parseQuery(query)));
                return;
            }
            if (pathOnly.equals("/app/square")) {
                escribir(out, AppServices.square(AppServices.parseQuery(query)));
                return;
            }
            if (pathOnly.equals("/app/time")) {
                escribir(out, AppServices.time());
                return;
            }
            if (pathOnly.equals("/app/health")) {
                escribir(out, AppServices.health());
                return;
            }
            // SERVICIO LENTO (punto 6.2): tarda a propósito
            if (pathOnly.equals("/app/slow")) {
                try { Thread.sleep(8000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                escribir(out, new AppServices.ServiceResult(200,
                        "application/json; charset=UTF-8", "{\"slept_ms\":8000}"));
                return;
            }

            // RECURSOS ESTÁTICOS (puntos 3 y 7)
            String path = URLDecoder.decode(pathOnly, StandardCharsets.UTF_8);
            if (path.equals("/")) path = "/index.html";

            String resource = normalizeResourcePath(path);
            if (resource == null) {
                responder(out, 403, "Forbidden", "text/html; charset=UTF-8",
                        errorHtml("403 Forbidden", "Ruta fuera del área pública."));
                return;
            }
            byte[] body = readResource(resource);
            if (body == null) {
                responder(out, 404, "Not Found", "text/html; charset=UTF-8",
                        errorHtml("404 Not Found", "No se encontró: " + escape(path)));
                return;
            }
            responder(out, 200, "OK", contentTypeByName(resource), body);

        } catch (IOException e) {
            System.err.println("   fallo leyendo/escribiendo: " + e.getMessage());
        }
    }

    /** Colapsa "." y ".." sin usar el sistema de archivos; null si intenta salir de la raíz. */
    private static String normalizeResourcePath(String path) {
        List<String> segs = new ArrayList<>();
        for (String seg : path.split("/")) {
            if (seg.isEmpty() || seg.equals(".")) continue;
            if (seg.equals("..")) {
                if (segs.isEmpty()) return null;
                segs.remove(segs.size() - 1);
            } else {
                segs.add(seg);
            }
        }
        return "/" + String.join("/", segs);
    }

    /** Lee del disco (desarrollo) o del classpath /public (dentro del jar). null si no existe. */
    private static byte[] readResource(String resource) throws IOException {
        Path devPath = WEB_ROOT.resolve("." + resource).normalize();
        if (devPath.startsWith(WEB_ROOT) && Files.exists(devPath) && !Files.isDirectory(devPath)) {
            return Files.readAllBytes(devPath);
        }
        try (InputStream is = SequentialHttpServer.class.getResourceAsStream("/public" + resource)) {
            return (is == null) ? null : is.readAllBytes();
        }
    }

    private static void escribir(OutputStream out, AppServices.ServiceResult r) throws IOException {
        responder(out, r.status(), reason(r.status()), r.contentType(),
                r.body().getBytes(StandardCharsets.UTF_8));
    }

    private static void responder(OutputStream out, int code, String reason,
                                  String contentType, byte[] body) throws IOException {
        String headers = "HTTP/1.1 " + code + " " + reason + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
    }

    private static String reason(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            default  -> "Status " + status;
        };
    }

    private static String contentTypeByName(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".html") || n.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (n.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (n.endsWith(".js"))   return "application/javascript";
        if (n.endsWith(".json")) return "application/json";
        if (n.endsWith(".png"))  return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".gif"))  return "image/gif";
        if (n.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream";
    }

    private static byte[] errorHtml(String titulo, String detalle) {
        String html = "<!doctype html><html lang=\"es\"><head><meta charset=\"UTF-8\">"
                + "<title>" + titulo + "</title></head><body>"
                + "<h1>" + titulo + "</h1><p>" + detalle + "</p></body></html>";
        return html.getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static int puertoDesde(String[] args, int porDefecto) {
        if (args.length > 0) return Integer.parseInt(args[0]);
        String env = System.getenv("PORT");
        return env != null ? Integer.parseInt(env) : porDefecto;
    }
}