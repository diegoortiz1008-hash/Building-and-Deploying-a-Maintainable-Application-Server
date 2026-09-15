package escuelaing.edu.co;

import java.util.HashMap;
import java.util.Map;


public class WebFramework {
    
    static Map<String, WebService> webServices = new HashMap();

    public static void get(String route, WebService ws){
        webServices.put(route,ws);
    }

    public static String invoke(String route){
        WebService ws = webServices.get(route);
        return ws.call();
    }
}
