package org.apache.tomee.webapp.jsf;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.tomee.webapp.jsf.JndiHelper.JndiTreeHelper.runOnJndiTree;

/**
 * @author rmannibucau
 */
@Named
@ApplicationScoped
public class JndiHelper {
    public static final String CONTEXT_NODE_TYPE = "context";
    public static final String LEAF_NODE_TYPE = "leaf";
    public static final String APPLICATION_NODE_TYPE = "application";
    public static final String MODULE_NODE_TYPE = "module";
    public static final String ROOT_NODE_TYPE = "root";

    private static final ContainerSystem CONTAINER_SYSTEM = SystemInstance.get().getComponent(ContainerSystem.class);

    public TreeNode getJndi() throws NamingException {
        final TreeNode root = new DefaultTreeNode(ROOT_NODE_TYPE, "/", null);

        for (AppContext appContext : CONTAINER_SYSTEM.getAppContexts()) {
            final TreeNode appNode = new DefaultTreeNode(APPLICATION_NODE_TYPE, appContext.getId(), root);

            // is there a simpler way?
            // id = guarantee unity
            final Map<String, ModuleContext> modules = new HashMap<String, ModuleContext>();
            for (BeanContext beanContext : appContext.getBeanContexts()) {
                if (!beanContext.getBeanClass().equals(BeanContext.Comp.class)) {
                    final ModuleContext moduleContext = beanContext.getModuleContext();
                    modules.put(moduleContext.getUniqueId(), moduleContext);
                }
            }

            for (ModuleContext module : modules.values()) {
                final TreeNode moduleNode = new DefaultTreeNode(MODULE_NODE_TYPE, appContext.getId(), appNode);
                addSubContext(module.getModuleJndiContext(), "module", moduleNode);
            }

            addSubContext(appContext.getAppJndiContext(), "app", appNode);
            addSubContext(appContext.getGlobalJndiContext(), "global", appNode);
        }

        return root;
    }

    private static void addSubContext(final Context context, final String subContext, final TreeNode parent) throws NamingException {
        final TreeNode subNode = new DefaultTreeNode("context", subContext, parent);
        runOnJndiTree((Context) context.lookup(subContext), subNode);
    }

    protected static class JndiTreeHelper {
        private Context context;
        private String path;

        private JndiTreeHelper(Context ctx, String name) {
            path = name;
            context = ctx;
        }

        private TreeNode runOnTree(final TreeNode root) {
            final NamingEnumeration<Binding> ne;
            try {
                ne = context.listBindings(path);
            } catch (NamingException e) {
                return root;
            }

            while (ne.hasMoreElements()) {
                final Binding current;
                try {
                    current = ne.next();
                } catch (NamingException nnfe) {
                    continue;
                }

                final String name = current.getName();
                final String fullName = path.concat("/").concat(name);
                final Object obj = current.getObject();

                if (obj != null && obj instanceof Context) {
                    runOnJndiTree(context, new DefaultTreeNode(CONTEXT_NODE_TYPE, name, root), fullName);
                } else {
                    new DefaultTreeNode(LEAF_NODE_TYPE, new Leaf(name, value(context, fullName)), root);
                }
            }
            return root;
        }

        private static Object value(Context context, String fullName) {
            try {
                return context.lookup(fullName);
            } catch (NamingException nnfe) {
                return null;
            }
        }

        private static TreeNode runOnJndiTree(final Context ctx, final TreeNode root, final String prefix) {
            return new JndiTreeHelper(ctx, prefix).runOnTree(root);
        }

        public static TreeNode runOnJndiTree(final Context ctx, final TreeNode root) {
            return new JndiTreeHelper(ctx, "").runOnTree(root);
        }

        public static class Leaf {
            private String name;
            private Object value;

            public Leaf(String name, Object value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public Object getValue() {
                return value;
            }
        }
    }
}
