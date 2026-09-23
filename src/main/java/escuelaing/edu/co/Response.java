package escuelaing.edu.co;

/** Optional response metadata set by a route before it returns its body. */
public final class Response {
    private int status = 200;
    private String contentType = "text/plain; charset=UTF-8";
    public Response status(int value) { status = value; return this; }
    public Response contentType(String value) { contentType = value; return this; }
    public int status() { return status; }
    public String contentType() { return contentType; }
}
