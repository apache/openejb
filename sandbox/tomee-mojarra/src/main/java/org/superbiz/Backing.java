package org.superbiz;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean
public class Backing {
    @Inject
    private CDIBean bean;

    private String name = "ok";

    public String getName() {
        return name + " " + bean.getCdi();
    }

    public void setName(String name) {
        this.name = name;
    }
}
