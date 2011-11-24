package org.apache.tomee.webapp.jsf;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.apache.tomee.webapp.jsf.JSFHelper.param;
import static org.apache.tomee.webapp.jsf.OpenEJBHelper.method;

/**
 * @author rmannibucau
 */
@Named("ejbInvocationHelper")
@SessionScoped
public class EJBInvocationHelper implements Serializable {
    @Inject @Named("invocation") @SessionScoped private Invocation invocation;
    private List<ParameterInfo> methodParameters;

    public List<ParameterInfo> getMethodParameters() {
        if (methodParameters == null) {
            final String app = param("app");
            final String name = param("name");
            final String rawId = param("methodId");

            methodParameters = new ArrayList<ParameterInfo>();
            if (app == null || name == null || rawId == null) {
                return methodParameters;
            }

            invocation.setApp(app);
            invocation.setName(name);
            invocation.setId(Long.parseLong(rawId));

            final Method mtd = method(invocation.getApp(), invocation.getName(), invocation.getId());
            if (mtd != null) {
                for (Class<?> parameter : mtd.getParameterTypes()) {
                    methodParameters.add(new ParameterInfo(parameter.getName().replace("java.lang.", "")));
                }
            }
        }
        return methodParameters;
    }

    public String submit() {
        invocation.getMethodParameters().clear();
        for (ParameterInfo info : methodParameters) {
            invocation.getMethodParameters().add(info.getValue());
        }
        methodParameters.clear();
        methodParameters = null;
        return "ejb-invoker?faces-redirect=true";
    }



    public static class ParameterInfo {
        private String value;
        private String type;

        public ParameterInfo(String type) {
            this.type = type;
            this.value = null;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
