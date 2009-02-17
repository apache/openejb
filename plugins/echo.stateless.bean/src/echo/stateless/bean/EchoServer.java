package echo.stateless.bean;

import javax.ejb.Remote;

@Remote
public interface EchoServer {

	public abstract String echo(String message);

}