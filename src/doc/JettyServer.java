import org.mortbay.util.*;
import org.mortbay.http.*;
import org.mortbay.jetty.servlet.*;


public class JettyServer
{
    //
    // Jetty server configuration.
    //

    // Location of static content.
    private static final String _DOC_ROOT = "c:/jetty/docroot";

    // Servlet URL mapping.
    private static final String _SERVLET_URL = "/servlet/*";

    // Log files.
    private static final String _LOGFILE =
        "c:/jetty/logs/jetty_yyyy_mm_dd.request.log";


    public static void main (String[] args)
    {
        try
        {
            // Create the server
            HttpServer server = new HttpServer();

            // Create a port listener on port 80
            HttpListener listener = server.addListener(new InetAddrPort (80));

            // Create a context
            HttpContext context = server.getContext(null,"/");

            // Normal content is in here
            context.setResourceBase(_DOC_ROOT);
            context.setServingResources (true);

            // 
            // The following code is commented out to resolve ClassLoader issues
            // when using OpenEJB within the Jetty HttpServer.
            //
            // context.setClassPath("path to servlet classes");

            // Create a servlet container
            ServletHandler handler = new ServletHandler();
            handler.setDynamicServletPathSpec(_SERVLET_URL);

            // add some logging
            NCSARequestLog log = new NCSARequestLog(_LOGFILE);
            server.setRequestLog(log);
         
            // Map Dump servlet onto the container
            handler.addServlet("Dump", "/Dump", "org.mortbay.servlet.Dump");
            context.addHttpHandler(handler);

            // Start the http server
            server.start ();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

