package org.apache.tomee.webapp.jsf;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.assembler.classic.JndiBuilder;
import org.apache.xbean.recipe.RecipeHelper;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.apache.tomee.webapp.jsf.OpenEJBHelper.baseClass;
import static org.apache.tomee.webapp.jsf.OpenEJBHelper.bean;
import static org.apache.tomee.webapp.jsf.OpenEJBHelper.method;

/**
 * @author rmannibucau
 */
@Named("ejbInvokerHelper")
public class EJBInvokerHelper {
    @Inject @Named("invocation") @SessionScoped private Invocation invocation;

    public String getInvoke() {
        if (invocation.isReady()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("invoke this EJB from EJB view, session was lost"));
            return "can't do invokation";
        }

        Object o = result();
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    private Object result() {
        final BeanContext context = bean(invocation.getApp(), invocation.getName());
        final Class<?> itf = baseClass(context);
        final InterfaceType type = context.getInterfaceType(itf);
        final String jndi = "openejb:Deployment/".concat(JndiBuilder.format(invocation.getName(), itf.getName(), type));
        final Object bean;
        try {
            bean = new InitialContext().lookup(jndi);
        } catch (NamingException e) {
            return exceptionToStr(e);
        }

        final Method mtd = method(invocation.getApp(), invocation.getName(), invocation.getId());
        try {
            return mtd.invoke(bean, getArgs(invocation.getMethodParameters(), mtd.getParameterTypes()));
        } catch (InvocationTargetException e) {
            return exceptionToStr(e.getCause());
        } catch (Exception e) {
            return exceptionToStr(e);
        }
    }

    private String exceptionToStr(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString().replace("\n", "<br/>");
    }

    private Object[] getArgs(List<String> methodParameters, Class<?>[] types) {
        Object[] obj = new Object[methodParameters.size()];
        int i = 0;
        for (String p : methodParameters) {
            obj[i] = convert(p, types[i]);
            i++;
        }
        return obj;
    }

    private Object convert(String p, Class<?> type) {
        return RecipeHelper.convert(type, p, true);
    }
}
