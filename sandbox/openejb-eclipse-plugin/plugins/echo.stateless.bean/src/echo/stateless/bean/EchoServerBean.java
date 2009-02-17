package echo.stateless.bean;

import javax.ejb.Stateless;

@Stateless
public class EchoServerBean implements EchoServer {

    public String echo(String message) {
	System.out.println("Client sent: " + message);
	return "Server: " + message;
    }
}
