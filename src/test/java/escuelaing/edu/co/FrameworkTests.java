package escuelaing.edu.co;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class FrameworkTests {
    @Test
    void requestExtractsAndDecodesMultipleQueryValues() {
        Request request = new Request("GET", "/hello", "name=Ada+Lovelace&language=en");
        assertEquals("Ada Lovelace", request.getValue("name"));
        assertEquals("en", request.getValue("language"));
        assertNull(request.getValue("missing"));
    }

    @Test
    void routerFindsRegisteredGetRoute() throws Exception {
        Router router = new Router();
        router.get("/pi", (request, response) -> "3.141592653589793");
        RouteHandler handler = router.findGet("/pi").orElseThrow();
        assertEquals("3.141592653589793", handler.handle(new Request("GET", "/pi", ""), new Response()));
        assertEquals(false, router.findGet("/missing").isPresent());
    }

    @Test
    void staticFilesIncludeTextAndBinaryResources() throws Exception {
        StaticFileService files = new StaticFileService();
        assertEquals("text/html; charset=UTF-8", files.find("/").contentType());
        assertEquals("text/css; charset=UTF-8", files.find("/styles.css").contentType());
        StaticFileService.StaticResource logo = files.find("/images/logo.png");
        assertNotNull(logo);
        assertEquals("image/png", logo.contentType());
        assertNotNull(logo.body());
    }
}
