package org.openejb.alt.config;

import org.openejb.alt.config.sys.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.OpenEJBException;
import org.openejb.util.Messages;
import org.openejb.util.FileUtils;
import java.util.Enumeration;
import java.util.Vector;
import java.io.PrintStream;
import java.io.DataInputStream;
import java.io.File;


/**
 * This class represents a command line tool for deploying beans.
 * 
 * At the moment it contains multiple println statements
 * and statements that read input from the user.
 * 
 * These statements are really in chunks in specific times throughout
 * the class.  These chunks could be refactored into methods. Then
 * the implementation of those methods could actually be delegated
 * to another class that implements a specific interface we create.
 * 
 * The command line statements could be moved into an implementation
 * of this new interface. We could then create another implementation
 * that gathers information from a GUI.
 * 
 * This would give us a Deploy API rather than just a command line
 * tool.  Then beans could be deployed programmatically by another
 * application, by a GUI screen, or by command line.
 * 
 * Note: The command line version should be finished first!!!  We
 * don't want to start on a crusade of abstracting code that doesn't
 * yet exist.  Functionality first, neat flexible stuff later.
 */
public class Deploy {

    private final String DEPLOYMENT_ID_HELP = "Deployment ID ----- \nA name for the ejb that is unique not only in this jar,    \nbut in all the jars in the container system.  This name    \nwill allow OpenEJB to place the bean in a global index and \nreference the bean quickly.  OpenEJB will also use this    \nname as the global JNDI name for application clients in the\nsame VM.  The integrating server may also use this name to \nas part of a global JNDI namespace available to remote     \napplication clients.\n\nExample: client/acme/bugsBunnyBean";
    private final String CONTAINER_ID_HELP = "TODO: Give some direction";
    private final String CONNECTOR_ID_HELP = "TODO: Give some direction";
    
    /*=======----------TODO----------=======
       Neat options that this Deploy tool 
       could support
      
       Contributions and ideas welcome!!!
      =======----------TODO----------=======*/
        
    /**
     * Idea for a command line option
     * 
     * If there is only one container of the appropriate type
     * for a bean then the bean is automatically assigned to that
     * container.  The user is notified unless the QUIET flag is true.
     * 
     * not implemented
     */
    private boolean AUTO_ASSIGN;
    
    /**
     * Idea for a command line option
     * 
     * -m   Move the jar to the OPENEJB_HOME/beans directory
     *
     * not implemented
     */
    private boolean MOVE_JAR;
    
    /**
     * Idea for a command line option
     * 
     * Will automatically create an OpenEJB configuration
     * file that can accomodate the beans in the jar.
     * 
     * If there already is a config file, but, for example, there 
     * is not a container that is compatable for a bean type in the
     * jar, then a useable container of the right type will be 
     * automatically created with default values.
     * 
     * not implemented
     */
    private boolean AUTO_CONFIG;

    /**
     * Idea for a command line option
     * 
     * Will generate the bean's deployment id from a particular id generation
     * strategy.
     * 
     * -g[S#]
     *             
     * S# can be a number key to a generation strategy that is 
     * looked up internally.
     * 
     * ----------------------------
     * One strategy could be:
     * id = jar_directory + ejb-name
     * example:
     * DIR   path/to/a/jarfile/myBeans.jar
     * BEAN  CustomerBean
     * ID    path/to/a/jarfile/CustomerBean
     * 
     * ----------------------------   
     * Another strategy:
     * Just use the ejb-name
     * DIR   doesnt/matter/path/to/a/jarfile/myBeans.jar
     * BEAN  CustomerBean
     * ID    CustomerBean
     * 
     * If ejb-name already looked like a JNDI name
     * then this would work great, otherwise there
     * would be a high chance of name collitions in
     * the OpenEJB IntraVM global namespace.
     * ----------------------------
     * 
     * not implemented
     */
    private boolean GENERATE_DEPLOYMENT_ID;
    
    
    /**
     * Idea for a command line option
     * 
     * Generate the CORBA stubs and ties
     * and add them to the jar so people don't
     * have to run a seperate tool to do that.
     * 
     * not implemented
     */
    private boolean GENERATE_STUBS;

    private DataInputStream in;
    private PrintStream out;
    private Openejb config;
    private String configFile;
    private boolean configChanged;
    private boolean autoAssign;
    private Container[] containers;
    private Connector[] resources;

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public Deploy() throws OpenEJBException {
    }

    public void init(String openejbConfigFile) throws OpenEJBException{
        try {
            in  = new DataInputStream(System.in); 
            out = System.out;

            configFile = openejbConfigFile;
            if (configFile == null) {
                try{
                    configFile = System.getProperty("openejb.configuration");
                } catch (Exception e){}
            }
            if (configFile == null) {
                configFile = ConfigUtils.searchForConfiguration();
            }
            config = ConfigUtils.readConfig(configFile);
            
            /* Load container list */
            containers = new Container[config.getContainerCount()];
            Enumeration enum = config.enumerateContainer();

            for ( int i=0; i < containers.length; i++ ) {
                containers[i] = (Container) enum.nextElement();
            }

            /* Load resource list */
            resources = new Connector[config.getConnectorCount()];
            enum = config.enumerateConnector();

            for ( int i=0; i < resources.length; i++ ) {
                resources[i] = (Connector) enum.nextElement();
            }

        } catch ( Exception e ) {
            // TODO: Better exception handling.
            e.printStackTrace();
            throw new OpenEJBException(e.getMessage());
        }

    }


    /*------------------------------------------------------*/
    /*    Methods for starting the deployment process       */
    /*------------------------------------------------------*/

    private void deploy(String jarLocation) throws OpenEJBException{
        EjbJar jar = ConfigUtils.readEjbJar(jarLocation);
        OpenejbJar openejbJar = new OpenejbJar();
        Bean[] beans = getBeans(jar);

        listBeanNames(beans);

        for ( int i=0; i < beans.length; i++ ) {
            openejbJar.addEjbDeployment(deployBean(beans[i]));
        }
        
        if (MOVE_JAR) {
            jarLocation = moveJar(jarLocation);
        }
        
        /* TODO: Automatically updating the users
        config file might not be desireable for
        some people.  We could make this a 
        configurable option. 
        */
        addDeploymentEntryToConfig(jarLocation);

        saveChanges(jarLocation, openejbJar);
        

    }

    private String moveJar(String jar) throws OpenEJBException{
        File origFile = new File(jar);
        
        // Safety checks
        if (!origFile.exists()){
            ConfigUtils.logWarning("deploy.m.010", origFile.getAbsolutePath());
            return jar;
        }

        if (origFile.isDirectory()){
            ConfigUtils.logWarning("deploy.m.020", origFile.getAbsolutePath());
            return jar;
        }

        if (!origFile.isFile()){
            ConfigUtils.logWarning("deploy.m.030", origFile.getAbsolutePath());
            return jar;
        }

        // Move file
        String jarName = origFile.getName();
        File beansDir = null;
        try {
            beansDir = FileUtils.getDirectory("beans"); 
        } catch (java.io.IOException ioe){
            ConfigUtils.logWarning("deploy.m.040", origFile.getAbsolutePath(), ioe.getMessage());
            return jar;
        }
        
        File newFile = new File(beansDir, jarName);
        boolean moved = false;
        
        try{
            moved = origFile.renameTo(newFile); 
        } catch (SecurityException se){
            ConfigUtils.logWarning("deploy.m.050", origFile.getAbsolutePath(), se.getMessage());
        }

        if ( moved ){
            return newFile.getAbsolutePath();
        } else {
            ConfigUtils.logWarning("deploy.m.060", origFile.getAbsolutePath(), newFile.getAbsoluteFile());
            return origFile.getAbsolutePath();
        }
    }

    private EjbDeployment deployBean(Bean bean) throws OpenEJBException{
        EjbDeployment deployment = new EjbDeployment();

        out.println("\n-----------------------------------------------------------");
        out.println("Deploying bean: "+bean.getEjbName());
        out.println("-----------------------------------------------------------");
        deployment.setEjbName( bean.getEjbName() );
        deployment.setDeploymentId( promptForDeploymentId() );
        deployment.setContainerId(  promptForContainerId(bean)  );

        if ( bean.getResourceRefCount() > 0 ) {
            ResourceRef[] refs = new ResourceRef[bean.getResourceRefCount()];
            Enumeration enum = bean.enumerateResourceRef();
            for ( int i=0; i < refs.length; i++ ) {
                refs[i] = (ResourceRef)enum.nextElement();
            }
            out.println("\n==--- Step 3 ---==");
            out.println("\nThis bean contains the following references to external \nresources:");

            out.println("\nName\t\t\tType\n");

            for ( int i=0; i < refs.length; i++ ) {
                out.print(refs[i].getResRefName()+"\t");
                out.println(refs[i].getResType());
            }

            out.println("\nThese references must be linked to the available resources\ndeclared in your config file.");

            out.println("Available resources are:");
            listResources(resources);
            for ( int i=0; i < refs.length; i++ ) {
                deployment.addResourceLink( resolveResourceRef( refs[i] ));
            }
        }

        return deployment;
    }


    /*------------------------------------------------------*/
    /*    Methods for deployment id mapping                 */
    /*------------------------------------------------------*/
    private void listBeanNames(Bean[] beans) {
        out.println("This jar contains the following beans:");
        for ( int i=0; i < beans.length; i++ ) {
            out.println("  "+beans[i].getEjbName());
        }
        out.println();
    }

    private String promptForDeploymentId() throws OpenEJBException{
        String answer = null;
        try {
            boolean replied = false;
            out.println("\n==--- Step 1 ---==");
            out.println("\nPlease specify a deployment id for this bean.");

            while ( !replied ) {
                out.println("Type the id or -help for more information.");
                out.print("\nDeployment ID: ");
                answer = in.readLine();        
                if ( "-help".equals( answer ) ) {
                    out.println(DEPLOYMENT_ID_HELP);
                } else if ( answer.length() > 0 ) {
                    replied = true;
                }
            }
        } catch ( Exception e ) {
            throw new OpenEJBException(e.getMessage());
        }
        return answer;
    }


    /*------------------------------------------------------*/
    /*    Methods for container mapping                     */
    /*------------------------------------------------------*/

    private String promptForContainerId(Bean bean) throws OpenEJBException{
        String answer = null;
        boolean replied = false;
        out.println("\n==--- Step 2 ---==");
        out.println("\nPlease specify which container the bean will run in.");
        out.println("Available containers are:");

        Container[] cs = getUsableContainers(bean);

        if ( cs.length == 0 ) {
            /* TODO: Allow or Automatically create a useable container
             * Stopping the deployment process because there is no
             * container of the right bean type is a terrible way
             * deal with the problem.  Instead, we should either 
             * 1) Automatically create a container for them and notify them
             *    that we have done so.
             * 2) Allow them to create their own container.
             * 3) Some combination of 1 and 2.
             */
            out.println("!! There are no "+bean.getType()+" containers declared in "+configFile+" !!");
            out.println("A "+bean.getType()+" container must be declared and \nconfigured in your configuration file before this jar can\nbe deployed.");
            System.exit(-1);
        } else if ( cs.length == 0 ) {
            /* TODO: Automatically assign the bean to the container
             * Since this is the only container in the system that 
             * can service this bean type, either 
             * 1) simply assign the bean to that container and notify the user.
             * 2) allow the user to create another container.
             */
        }

        listContainers(cs);
        int choice = 0;
        try {

            while ( !replied ) {
                out.println("\nType the number of the container\n-options to view the list again\nor -help for more information.");
                out.print("\nContainer: ");
                answer = in.readLine();        
                if ( "-help".equals( answer ) ) {
                    out.println(CONTAINER_ID_HELP);
                } else if ( "-options".equals( answer ) ) {
                    listContainers(cs);
                } else if ( answer.length() > 0 ) {
                    try {
                        choice = Integer.parseInt(answer);
                    } catch ( NumberFormatException nfe ) {
                        out.println("\'"+answer+"\' is not a numer.");
                        continue;
                    }
                    if ( choice > cs.length || choice < 1 ) {
                        out.println(choice+" is not an option.");
                        continue;
                    }
                    replied = true;
                }
            }
        } catch ( Exception e ) {
            throw new OpenEJBException(e.getMessage());
        }
        return cs[choice-1].getId();
    }


    private Container[] getUsableContainers(Bean bean) {
        Vector c = new Vector();        

        for ( int i=0; i < containers.length; i++ ) {
            if ( containers[i].getCtype().equals(bean.getType()) ) {
                c.add(containers[i]);
            }
        }

        Container[] useableContainers = new Container[c.size()];
        c.copyInto(useableContainers);

        return useableContainers;
    }

    private void listContainers(Container[] containers) {
        out.println("\nNum \tType     \tID\n");

        for ( int i=0; i < containers.length; i++ ) {
            out.print((i+1)+"\t");
            out.print(containers[i].getCtype()+"\t");
            out.println(containers[i].getId());
        }
    }


    /*------------------------------------------------------*/
    /*    Methods for connection(resource) mapping          */
    /*------------------------------------------------------*/
    private ResourceLink resolveResourceRef(ResourceRef ref) throws OpenEJBException{
        String answer = null;
        boolean replied = false;

        out.println("\nPlease link reference: "+ref.getResRefName());

        if ( resources.length == 0 ) {
            /* TODO: 1, 2 or 3
             * 1) Automatically create a connector and link the reference to it.
             * 2) Something more creative
             * 3) Some ultra flexible combination of 1 and 2.
             */
            out.println("!! There are no resources declared in "+configFile+" !!");
            out.println("A resource connector must be declared and configured in \nyour configuration file before this jar can be deployed.");
            System.exit(-2);
        } else if ( resources.length == 0 ) {
            /* TODO: 1, 2 or 3
             * 1) Automatically link the reference to the connector
             * 2) Something more creative
             * 3) Some ultra flexible combination of 1 and 2.
             */
        }

        int choice = 0;
        try {
            while ( !replied ) {
                out.println("\nType the number of the resource to link the bean's \nreference to, -options to view the list again, or -help\nfor more information.");
                out.print("\nResource: ");
                answer = in.readLine();        
                if ( "-help".equals( answer ) ) {
                    out.println(CONNECTOR_ID_HELP);
                } else if ( "-options".equals( answer ) ) {
                    listResources(resources);
                } else if ( answer.length() > 0 ) {
                    try {
                        choice = Integer.parseInt(answer);
                    } catch ( NumberFormatException nfe ) {
                        out.println("\'"+answer+"\' is not a numer.");
                        continue;
                    }
                    if ( choice > resources.length || choice < 1 ) {
                        out.println(choice+" is not an option.");
                        continue;
                    }
                    replied = true;
                }
            }
        } catch ( Exception e ) {
            throw new OpenEJBException(e.getMessage());
        }

        ResourceLink link = new ResourceLink();
        link.setResRefName( ref.getResRefName() );
        link.setResId(resources[choice-1].getId());
        return link;
    }

    private void listResources(Connector[] connectors) {
        out.println("\nNum \tID\n");

        for ( int i=0; i < connectors.length; i++ ) {
            out.print((i+1)+"\t");
            out.println(connectors[i].getId());
        }
    }



    private void addDeploymentEntryToConfig(String jarLocation){
        Enumeration enum = config.enumerateDeployments();
        File jar = new File(jarLocation);

        /* Check to see if the entry is already listed */
        while ( enum.hasMoreElements() ) {
            Deployments d = (Deployments)enum.nextElement();
            
            if ( d.getJar() != null ) {
                try {
                    File target = FileUtils.getFile(d.getJar(), false);
                    
                    /* 
                     * If the jar entry is already there, no need 
                     * to add it to the config or go any futher.
                     */
                    if (jar.equals(target)) return;
                } catch (java.io.IOException e){
                    /* No handling needed.  If there is a problem
                     * resolving a config file path, it is better to 
                     * just add this jars path explicitly.
                     */
                }
            } else if ( d.getDir() != null ) {
                try {
                    File target = FileUtils.getFile(d.getDir(), false);
                    File jarDir = jar.getAbsoluteFile().getParentFile();

                    /* 
                     * If a dir entry is already there, the jar
                     * will be loaded automatically.  No need 
                     * to add it explicitly to the config or go
                     * any futher.
                     */
                    if (jarDir != null && jarDir.equals(target)) return;
                } catch (java.io.IOException e){
                    /* No handling needed.  If there is a problem
                     * resolving a config file path, it is better to 
                     * just add this jars path explicitly.
                     */
                }
            }
        }

        /* Create a new Deployments entry */
        Deployments dep = new Deployments();
        dep.setJar(jarLocation);
        config.addDeployments(dep);
        configChanged = true;
    }


    private void saveChanges(String jarFile, OpenejbJar openejbJar) throws OpenEJBException{
        out.println("\n-----------------------------------------------------------");
        out.println("Done collecting deployment information!");
        
        out.print("Creating the openejb-jar.xml file...");
        ConfigUtils.writeOpenejbJar("META-INF/openejb-jar.xml", openejbJar);
        
        out.println("done");

        out.print("Writing openejb-jar.xml to the jar...");
        ConfigUtils.addFileToJar(jarFile, "META-INF/openejb-jar.xml");
        
        out.println("done");
        
        if (configChanged) {
            out.print("Updating your system config...");
            ConfigUtils.writeConfig(configFile,config);
            
            out.println("done");
        }

        out.println("\nCongratulations! Your jar is ready to use with OpenEJB.");
        
        out.println("\nNOTE: If you move or rename your jar file, you will have to\nupdate the path in this jar's deployment entry in your \nOpenEJB config file.");

    }


    /*------------------------------------------------------*/
    /*    Methods for exception handling                    */
    /*------------------------------------------------------*/
    private void logException(String m) throws OpenEJBException{
        System.out.println("[OpenEJB] "+m);
        throw new OpenEJBException(m);
    }

    private void logException(String m, Exception e) throws OpenEJBException{
        m += " : "+e.getMessage();
        //System.out.println("[OpenEJB] "+m);
        //e.printStackTrace();
        throw new OpenEJBException(m);
    }

    /*------------------------------------------------------*/
    /*    Static methods                                    */
    /*------------------------------------------------------*/

    public static void main(String args[]) {
        try {
            Deploy d = new Deploy();

            for (int i=0; i < args.length; i++){
                //AUTODEPLOY
                if (args[i].equals("-a")){
                    d.AUTO_ASSIGN = true;
                    d.GENERATE_DEPLOYMENT_ID = true;
                } else if (args[i].equals("-m")){
                    d.MOVE_JAR = true;
                } else if (args[i].equals("-c")){
                    if (args.length > i+2 ) {
                        System.setProperty("openejb.configuration", args[++i]);
                    }
                } else if (args[i].equals("-l")){
                    if (args.length > i+2 ) {
                        System.setProperty("log4j.configuration", args[++i]);
                    }
                } else if (args[i].equals("-d")){
                    if (args.length > i+2 ) {
                        System.setProperty("openejb.home", args[++i]);
                    }
                } else {
                    // We must have reached the jar list
                    d.init(null);
                    for (int j=i; j < args.length; j++){
                        d.deploy( args[j] );
                    }
                }
            }
                        
       } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private static void usage() {
        String usage = ""+
                       "OpenEJB Deployer\n"+
                       "\n"+
                       "Usage: Deploy configfile jar-file\n"+
                       "\n"+
                       "configfile - The OpenEJB configuration file\n"+
                       "    for your container system.\n"+
                       "jar-file   - The beans jar file containing the \n"+
                       "    the ejb-jar.xml deployment descriptor.\n"+
                       "\n"+
                       "Example:\n"+
                       "    Deploy myOpenEJB.conf myBeans.jar\n";
        System.out.print(usage);
    }


    /*------------------------------------------------------*/
    /*    Methods for collecting beans                      */
    /*------------------------------------------------------*/
    private Bean[] getBeans(EjbJar jar) {
        Enumeration beanItemList = jar.getEnterpriseBeans().enumerateEnterpriseBeansItem();
        Vector beanList = new Vector();
        while ( beanItemList.hasMoreElements() ) {
            EnterpriseBeansItem item = (EnterpriseBeansItem)beanItemList.nextElement();
            if ( item.getEntity() == null ) {
                beanList.add(new SessionBean(item.getSession()));
            } else {
                beanList.add(new EntityBean(item.getEntity()));
            }
        }
        Bean[] beans = new Bean[beanList.size()];
        beanList.copyInto(beans);
        return beans;
    }




    /*------------------------------------------------------*/
    /*    Inner Classes for easy bean collections           */
    /*------------------------------------------------------*/
    
    interface Bean {

        public final String BMP_ENTITY = "BMP_ENTITY";
        public final String CMP_ENTITY = "CMP_ENTITY";
        public final String STATEFUL   = "STATEFUL";
        public final String STATELESS  = "STATELESS";


        public Enumeration enumerateResourceRef();
        public String getEjbName();
        public int getResourceRefCount();
        public String getType();
    }

    class EntityBean implements Bean {
        Entity bean;
        String type;
        EntityBean(Entity bean) {
            this.bean = bean;
            if ( bean.getPersistenceType().equals("Container") ) {
                type = CMP_ENTITY;
            } else {
                type = BMP_ENTITY;
            }
        }

        public Enumeration enumerateResourceRef() {
            return bean.enumerateResourceRef();
        }

        public String getEjbName() {
            return bean.getEjbName();
        }

        public int getResourceRefCount() {
            return bean.getResourceRefCount();
        }

        public String getType() {
            return type;
        }
    }

    class SessionBean implements Bean {

        Session bean;
        String type;

        SessionBean(Session bean) {
            this.bean = bean;
            if ( bean.getSessionType().equals("Stateful") ) {
                type = STATEFUL;
            } else {
                type = STATELESS;
            }
        }

        public Enumeration enumerateResourceRef() {
            return bean.enumerateResourceRef();
        }

        public String getEjbName() {
            return bean.getEjbName();
        }

        public int getResourceRefCount() {
            return bean.getResourceRefCount();
        }

        public String getType() {
            return type;
        }
    }

}
