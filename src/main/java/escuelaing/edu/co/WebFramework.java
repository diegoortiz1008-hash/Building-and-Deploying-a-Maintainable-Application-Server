package escuelaing.edu.co;

import java.io.IOException;

/** Public API used by an application to configure and start the framework. */
public final class WebFramework {
    private static final Router ROUTER = new Router();
    private static final StaticFileService STATIC_FILES = new StaticFileService();
    private static final HttpServer SERVER = new HttpServer(ROUTER, STATIC_FILES);
    private WebFramework() { }
    public static void get(String route, RouteHandler handler) { ROUTER.get(route, handler); }
    public static void staticfiles(String root) { STATIC_FILES.configure(root); }
    public static void start() throws IOException { start(portFromEnvironment()); }
    public static void start(int port) throws IOException { SERVER.start(port); }
    public static void stop() { SERVER.stop(); }
    private static int portFromEnvironment() {
        String configuredPort = System.getenv("PORT");
        return configuredPort == null || configuredPort.isBlank() ? 8080 : Integer.parseInt(configuredPort);
    }
}
