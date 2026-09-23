package escuelaing.edu.co;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Sequential HTTP infrastructure. It processes one connection at a time. */
public final class HttpServer {
    private final Router router;
    private final StaticFileService staticFiles;
    private volatile boolean running;

    public HttpServer(Router router, StaticFileService staticFiles) {
        this.router = router;
        this.staticFiles = staticFiles;
    }

    public void start(int port) throws IOException {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Application server listening on port " + port);
            while (running) {
                try (Socket client = serverSocket.accept()) {
                    handle(client);
                } catch (IOException exception) {
                    System.err.println("Could not process request: " + exception.getMessage());
                }
            }
        }
        System.out.println("Server stopped gracefully.");
    }

    public void stop() { running = false; }

    private void handle(Socket client) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        OutputStream output = client.getOutputStream();
        String requestLine = input.readLine();
        discardHeaders(input);
        if (requestLine == null || requestLine.isBlank()) { writeText(output, 400, "Bad Request", "400 Bad Request"); return; }
        String[] parts = requestLine.trim().split("\\s+");
        if (parts.length != 3 || !parts[2].startsWith("HTTP/")) { writeText(output, 400, "Bad Request", "400 Bad Request"); return; }
        if (!"GET".equals(parts[0])) { writeText(output, 405, "Method Not Allowed", "405 Method Not Allowed"); return; }

        String rawTarget = parts[1];
        int queryAt = rawTarget.indexOf('?');
        String path = decode(queryAt < 0 ? rawTarget : rawTarget.substring(0, queryAt));
        String query = queryAt < 0 ? "" : rawTarget.substring(queryAt + 1);
        RouteHandler handler = router.findGet(path).orElse(null);
        if (handler != null) {
            Response response = new Response();
            try { write(output, response, handler.handle(new Request("GET", path, query), response)); }
            catch (Exception exception) { writeText(output, 500, "Internal Server Error", "500 Internal Server Error"); }
            return;
        }
        StaticFileService.StaticResource resource = staticFiles.find(path);
        if (resource == null) { writeText(output, 404, "Not Found", "404 Not Found"); return; }
        write(output, 200, "OK", resource.contentType(), resource.body());
    }

    private void discardHeaders(BufferedReader input) throws IOException {
        String header;
        while ((header = input.readLine()) != null && !header.isEmpty()) { }
    }
    private String decode(String value) { return URLDecoder.decode(value, StandardCharsets.UTF_8); }
    private void write(OutputStream output, Response response, String body) throws IOException {
        write(output, response.status(), reason(response.status()), response.contentType(), body.getBytes(StandardCharsets.UTF_8));
    }
    private void writeText(OutputStream output, int status, String reason, String body) throws IOException {
        write(output, status, reason, "text/plain; charset=UTF-8", body.getBytes(StandardCharsets.UTF_8));
    }
    private void write(OutputStream output, int status, String reason, String type, byte[] body) throws IOException {
        String headers = "HTTP/1.1 " + status + " " + reason + "\r\nContent-Type: " + type + "\r\nContent-Length: " + body.length + "\r\nConnection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII)); output.write(body); output.flush();
    }
    private String reason(int status) { return switch (status) { case 200 -> "OK"; case 400 -> "Bad Request"; case 404 -> "Not Found"; case 405 -> "Method Not Allowed"; default -> "Internal Server Error"; }; }
}
