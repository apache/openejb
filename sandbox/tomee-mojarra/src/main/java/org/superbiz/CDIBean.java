package org.superbiz;

import javax.inject.Named;

@Named
public class CDIBean {
    public String getCdi() {
        return "cdi";
    }
}
