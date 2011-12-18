package org.apache.tomee.webapp.jsf;

import javax.faces.context.FacesContext;

public final class JSFHelper {
    private JSFHelper() {
        // no-op
    }

    public static String param(String name) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
    }
}
