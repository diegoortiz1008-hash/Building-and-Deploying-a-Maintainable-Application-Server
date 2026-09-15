package escuelaing.edu.co;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import escuelaing.edu.co.http.HttpServer;


public class WebFramework {
    
    static Map<String, WebService> webServices = new HashMap();


    public static void get(String route, WebService ws){
        webServices.put(route,ws);
    }

    public static String invoke(String route){
        WebService ws = webServices.get(route);
        return ws.call();
    }

    public static void start(){

        String[] args = {};
        Object level;
        Object logger;
        
        try{
            HttpServer.main(args);
        } catch (IOException ex){
            logger.getLonger(WebFramework.class.getName()).log(level.SEVERE, null, ex);
        }catch (URISyntaxException ex){
            logger.getLonger(WebFramework.class.getName()).log(level.SEVERE, null, ex);
        }
    }
}
