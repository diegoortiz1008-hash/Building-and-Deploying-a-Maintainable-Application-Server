package escuelaing.edu.co;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable information available to a route handler. */
public final class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryValues;
    public Request(String method, String path, String query) {
        this.method = method; this.path = path; this.queryValues = Collections.unmodifiableMap(parseQuery(query));
    }
    public String method() { return method; }
    public String path() { return path; }
    public String getValue(String name) { return queryValues.get(name); }
    public Map<String, String> queryValues() { return queryValues; }
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> values = new LinkedHashMap<>();
        if (query == null || query.isBlank()) return values;
        for (String pair : query.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator < 0 ? pair : pair.substring(0, separator);
            String value = separator < 0 ? "" : pair.substring(separator + 1);
            values.put(decode(key), decode(value));
        }
        return values;
    }
    private static String decode(String value) { return URLDecoder.decode(value, StandardCharsets.UTF_8); }
}
