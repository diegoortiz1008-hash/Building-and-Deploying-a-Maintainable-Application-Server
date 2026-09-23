package escuelaing.edu.co;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads static files after no dynamic route has matched. */
public final class StaticFileService {
    private String root = "/webroot";

    public void configure(String root) {
        if (root == null || root.isBlank()) throw new IllegalArgumentException("Static file root is required.");
        this.root = root.startsWith("/") ? root : "/" + root;
    }

    public StaticResource find(String requestPath) throws IOException {
        String resourcePath = requestPath.equals("/") ? "/index.html" : requestPath;
        if (!isSafe(resourcePath)) return null;
        String externalRoot = System.getenv("STATIC_FILES_PATH");
        if (externalRoot != null && !externalRoot.isBlank()) {
            Path base = Path.of(externalRoot).toAbsolutePath().normalize();
            Path candidate = base.resolve(resourcePath.substring(1)).normalize();
            if (candidate.startsWith(base) && Files.isRegularFile(candidate)) {
                return new StaticResource(Files.readAllBytes(candidate), contentType(resourcePath));
            }
        }
        try (InputStream input = StaticFileService.class.getResourceAsStream(root + resourcePath)) {
            return input == null ? null : new StaticResource(input.readAllBytes(), contentType(resourcePath));
        }
    }

    private boolean isSafe(String path) { return path.startsWith("/") && !path.contains("..") && !path.contains("\\"); }

    private String contentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    public record StaticResource(byte[] body, String contentType) { }
}
