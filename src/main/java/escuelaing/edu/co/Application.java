package escuelaing.edu.co;

import static escuelaing.edu.co.WebFramework.*;

/** Example application: only this class contains application-specific behaviour. */
public final class Application {
    private Application() { }
    public static void main(String[] args) throws Exception {
        staticfiles("/webroot");
        get("/hello", (request, response) -> {
            String name = request.getValue("name");
            if (name == null || name.isBlank()) name = "world";
            String prefix = System.getenv().getOrDefault("GREETING_PREFIX", "Hello");
            return prefix + " " + name;
        });
        get("/pi", (request, response) -> String.valueOf(Math.PI));
        if ("development".equals(System.getenv().getOrDefault("APP_ENV", "development"))) {
            get("/shutdown", (request, response) -> { stop(); return "Server will stop after this response."; });
        }
        start();
    }
}
