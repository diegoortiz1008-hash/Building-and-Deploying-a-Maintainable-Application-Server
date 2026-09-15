package escuelaing.edu.co;
import static escuelaing.edu.co.WebFramework.*;

public class Applicaction {

    public static void main(String[] args) throws Exception {

        staticfiles("/webroot");

        get("/hello", (req, resp) -> {
            String name = req.getValue("name");

            if (name == null || name.isBlank()) {
                name = "world";
            }

            return "Hello " + name;
        });

        get("/pi", (req, resp) ->
                String.valueOf(Math.PI));

        start();
    }
}
