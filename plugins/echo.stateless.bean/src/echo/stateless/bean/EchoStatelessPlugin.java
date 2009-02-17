package echo.stateless.bean;

import org.apache.openejb.eclipse.OpenEjbApplication;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EchoStatelessPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "echo.stateless.bean";

    // The shared instance
    private static EchoStatelessPlugin plugin;

    /**
     * The constructor
     */
    public EchoStatelessPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
	super.start(context);
	plugin = this;
	Bundle bundle = context.getBundle();
	OpenEjbApplication application = new OpenEjbApplication(bundle);
	context.registerService(OpenEjbApplication.class.getName(),
		application, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
	plugin = null;
	super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static EchoStatelessPlugin getDefault() {
	return plugin;
    }

}
