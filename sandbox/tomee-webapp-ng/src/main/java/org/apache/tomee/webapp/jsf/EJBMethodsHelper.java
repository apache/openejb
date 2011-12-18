package org.apache.tomee.webapp.jsf;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static org.apache.tomee.webapp.jsf.JSFHelper.param;
import static org.apache.tomee.webapp.jsf.OpenEJBHelper.app;
import static org.apache.tomee.webapp.jsf.OpenEJBHelper.baseClass;
import static org.apache.tomee.webapp.jsf.OpenEJBHelper.methods;

@Named("ejbMethodsHelper")
public class EJBMethodsHelper {
    private List<OpenEJBHelper.MethodInfo> dataTable;

    public void init() {
        final String app = param("app");
        final String name = param("name");

        if (app == null || name == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("app and ejb name are mandatory"));
        }

        final AppContext appContext = app(app);
        if (appContext != null) {
            dataTable = new ArrayList<OpenEJBHelper.MethodInfo>();
            for (BeanContext beanContext : appContext.getBeanContexts()) {
                if (beanContext.getDeploymentID().equals(name)) {
                    dataTable = methods(baseClass(beanContext));
                }
            }
        }
    }


    public List<OpenEJBHelper.MethodInfo> getDataTable() {
        if (dataTable == null) {
            init();
        }
        return dataTable;
    }

    public void setDataTable(List<OpenEJBHelper.MethodInfo> dataTable) {
        this.dataTable = dataTable;
    }
}
