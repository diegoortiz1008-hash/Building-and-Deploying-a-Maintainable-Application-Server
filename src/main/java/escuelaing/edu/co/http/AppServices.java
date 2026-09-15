package edu.escuelaing.arep;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Punto 4 — Lógica de los servicios "quemados" (hardcoded).
 * Solo funciones puras: reciben parámetros y devuelven un ServiceResult
 * (estado, tipo, cuerpo JSON). No dependen de la red, así que se prueban con JUnit.
 */
public final class AppServices {

    private AppServices() { }

    /** Respuesta de un servicio: estado HTTP, Content-Type y cuerpo. */
    public record ServiceResult(int status, String contentType, String body) { }

    private static final String JSON = "application/json; charset=UTF-8";

    /** /app/hello?name=...  ->  {"greeting":"Hola, <name>!"} */
    public static ServiceResult hello(Map<String, String> params) {
        String name = params.get("name");
        if (name == null || name.isBlank()) {
            return error(400, "Falta el parámetro 'name'.");
        }
        String body = "{\"greeting\":\"Hola, " + jsonEscape(name.trim()) + "!\"}";
        return new ServiceResult(200, JSON, body);
    }

    /** /app/square?x=...  ->  {"input":x,"square":x*x} */
    public static ServiceResult square(Map<String, String> params) {
        String x = params.get("x");
        if (x == null || x.isBlank()) {
            return error(400, "Falta el parámetro 'x'.");
        }
        double n;
        try {
            n = Double.parseDouble(x.trim());
        } catch (NumberFormatException e) {
            return error(400, "El parámetro 'x' no es un número: " + jsonEscape(x));
        }
        String body = "{\"input\":" + n + ",\"square\":" + (n * n) + "}";
        return new ServiceResult(200, JSON, body);
    }

    /** /app/time  ->  {"time":"...ISO...","epochMillis":...} */
    public static ServiceResult time() {
        String iso = OffsetDateTime.now().toString();
        String body = "{\"time\":\"" + jsonEscape(iso) + "\",\"epochMillis\":"
                + System.currentTimeMillis() + "}";
        return new ServiceResult(200, JSON, body);
    }

    /** /app/health  ->  {"status":"UP"} */
    public static ServiceResult health() {
        return new ServiceResult(200, JSON, "{\"status\":\"UP\"}");
    }

    private static ServiceResult error(int status, String message) {
        return new ServiceResult(status, JSON, "{\"error\":\"" + jsonEscape(message) + "\"}");
    }

    /** Parsea "a=1&b=hola%20mundo" en un mapa, decodificando clave y valor. */
    public static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) return params;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                params.put(decode(pair), "");
            } else {
                params.put(decode(pair.substring(0, eq)), decode(pair.substring(eq + 1)));
            }
        }
        return params;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    /** Escapa un valor para insertarlo con seguridad dentro de una cadena JSON. */
    public static String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}