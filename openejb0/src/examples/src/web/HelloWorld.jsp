<%@ page import="org.acme.hello.HelloObject,
                 org.acme.hello.HelloHome,
                 javax.naming.InitialContext,
				 javax.rmi.PortableRemoteObject" %>
<html>
<head>
	<title>OpenEJB -- EJB for Tomcat JSP</title>
</head>

<body>
<%
	String message = "No Joy";

    try {
		Object obj = new InitialContext().lookup("java:openejb/ejb/Hello");
		HelloHome ejbHome = (HelloHome)PortableRemoteObject.narrow(obj, HelloHome.class);
		HelloObject ejbObject = ejbHome.create();
        
		//The part we've all been waiting for...
		message = ejbObject.sayHello();

    } catch (Exception e) {
		out.print("Ouch! "+e.toString());
		e.printStackTrace();
    }
%>
<h1><%=message%></h1>
</body>
</html>

