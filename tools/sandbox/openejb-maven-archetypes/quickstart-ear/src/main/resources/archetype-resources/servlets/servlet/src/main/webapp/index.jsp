<%@ page import="javax.naming.InitialContext" %>
<%@ page import="org.superbiz.CalculatorRemote" %>

<html>
<body>
<%

    InitialContext context = new InitialContext();
    org.superbiz.CalculatorRemote calc = (org.superbiz.CalculatorRemote) context.lookup("java:comp/env/CalculatorBean");

    String val1 = request.getParameter("value1");
    String val2 = request.getParameter("value2");

    if (val1 != null && val1.length() > 0 && val2 != null && val2.length() > 0) {
        int v1 = 0;

        try {
            v1 = Integer.parseInt(val1);
        } catch (NumberFormatException e) {
        }

        int v2 = 0;

        try {
            v2 = Integer.parseInt(val2);
        } catch (NumberFormatException e) {
        }

%>

Result: <%= calc.add(v1, v2) %>

<%
    }
%>

<form action="index.jsp" method="POST">
    <input type="text" name="value1"/> + 
    <input type="text" name="value2"/>
    <input type="submit"/>
</form>

</body>
</html>
