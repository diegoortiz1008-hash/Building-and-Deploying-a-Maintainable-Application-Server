package escuelaing.edu.co;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Routes application behaviour without exposing socket handling to applications. */
public final class Router {
    private final Map<String, RouteHandler> getRoutes = new HashMap<>();
    public void get(String path, RouteHandler handler) {
        if (path == null || !path.startsWith("/")) throw new IllegalArgumentException("A route must start with '/'.");
        getRoutes.put(path, handler);
    }
    public Optional<RouteHandler> findGet(String path) { return Optional.ofNullable(getRoutes.get(path)); }
}
