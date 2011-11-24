package org.apache.tomee.webapp.jsf;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rmannibucau
 */
@Named("invocation")
@SessionScoped
public class Invocation implements Serializable {
    private String app;
    private String name;
    private long id;
    private List<String> methodParameters = new ArrayList<String>();

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getMethodParameters() {
        return methodParameters;
    }

    public boolean isReady() {
        return app != null && name != null && id > 0;
    }
}
