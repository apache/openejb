package org.apache.tomee.webapp.jsf;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.NamingException;
import java.util.List;

/**
 * @author rmannibucau
 */
@Named("ejbHelper")
@ApplicationScoped
public class EJBHelper {
    public static final String EMPTY_NODE_TYPE = "empty";
    public static final String EJB_NODE_TYPE = "ejb";
    public static final String APPLICATION_NODE_TYPE = "application";
    public static final String ROOT_NODE_TYPE = "root";

    private static final ContainerSystem CONTAINER_SYSTEM = SystemInstance.get().getComponent(ContainerSystem.class);

    public TreeNode getEJB() throws NamingException {
        final TreeNode root = new DefaultTreeNode(ROOT_NODE_TYPE, "/", null);
        for (AppContext appContext : CONTAINER_SYSTEM.getAppContexts()) {
            final String appName = appContext.getId();
            final TreeNode appNode = new DefaultTreeNode(APPLICATION_NODE_TYPE, appName, root);
            final List<BeanContext> contexts = appContext.getBeanContexts();
            if (!contexts.isEmpty()) {
                for (BeanContext beanContext : appContext.getBeanContexts()) {
                    if (beanContext.getBeanClass().equals(BeanContext.Comp.class)) {
                        continue;
                    }

                    final EJBInfo info = new EJBInfo(appName, (String) beanContext.getDeploymentID(), beanContext.getBeanClass().getName());
                    new DefaultTreeNode(EJB_NODE_TYPE, info, appNode);
                }
            } else {
                new DefaultTreeNode(EMPTY_NODE_TYPE, "no ejb", appNode);
            }
        }
        return root;
    }

    public static class EJBInfo {
        private String app;
        private String name;
        private String classname;

        public EJBInfo(final String app, final String name, final String classname) {
            this.app = app;
            this.name = name;
            this.classname = classname;
        }

        public String getName() {
            return name;
        }

        public String getClassname() {
            return classname;
        }

        public String getApp() {
            return app;
        }
    }
}
