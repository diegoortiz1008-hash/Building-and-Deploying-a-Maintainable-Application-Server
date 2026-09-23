package escuelaing.edu.co;

@FunctionalInterface
public interface RouteHandler {
    String handle(Request request, Response response) throws Exception;
}
