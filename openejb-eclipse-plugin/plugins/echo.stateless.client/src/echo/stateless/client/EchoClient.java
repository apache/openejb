package echo.stateless.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import echo.stateless.bean.EchoServer;

public class EchoClient {

    private EchoServer server;

    public EchoClient() {
	try {
	    Properties p = new Properties();
	    p.put(Context.INITIAL_CONTEXT_FACTORY,
		    "org.apache.openejb.client.LocalInitialContextFactory");
	    InitialContext initialContext = new InitialContext(p);
	    server = (EchoServer) initialContext.lookup("EchoServerBeanRemote");
	} catch (NamingException e) {
	    server = null;
	}
    }

    public String echo(String message) {
	return server.echo(message);
    }
}
