package org.openejb.ri.server;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

public interface DynamicContext extends Context{

    public void init(Hashtable env) throws NamingException;

}
