package org.acme.hello;

import java.rmi.RemoteException;
import javax.ejb.*;

public class HelloBean implements SessionBean {
    private SessionContext sessionContext;

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void setSessionContext(SessionContext sessionContext) {
	this.sessionContext = sessionContext;
    }

    public String sayHello() throws java.rmi.RemoteException {
	return "Hello World!!!!!";
    }
}


