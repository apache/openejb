package org.openejb.alt.config;

import org.openejb.alt.config.sys.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.OpenEJBException;
import java.util.Enumeration;
import java.util.Vector;
import java.io.PrintStream;
import java.io.DataInputStream;


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
     * Temp dir, Stubs & ties will be stored to this directory.
     * It will be deleted after the stub & tie class file have been packed into
     * the src EJB jar file.
     */
    protected static final String TEMP_DIR="deploy_temp";
    
    private static boolean DELETE_TEMP_DIR=false;

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
    private static boolean GENERATE_STUBS = false;

    private static boolean GENERATE_TIES = false;


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
    public Deploy(String openejbConfigFile) throws OpenEJBException {
        try {
            in = new DataInputStream(System.in);
            out = System.out;

            configFile = openejbConfigFile;
            config = ConfigUtils.readConfig(openejbConfigFile);

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

        /** list beans */
        listBeanNames(beans);

        for ( int i=0; i < beans.length; i++ ) {
            openejbJar.addEjbDeployment(deployBean(beans[i]));
        }

        /* TODO: Automatically updating the users
        config file might not be desireable for
        some people.  We could make this a
        configurable option.
        */
        addDeploymentEntryToConfig(jarLocation);
        saveChanges(jarLocation, openejbJar);

        if ( this.GENERATE_STUBS || this.GENERATE_TIES )
            genStubTie(jarLocation);
	
        finishedInfo();

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

        while ( enum.hasMoreElements() ) {
            Deployments d = (Deployments)enum.nextElement();
            if (jarLocation.equals(d.getJar())) {
                /* It's already there, no need to add it
                 * or go any futher.
                 */
                return;
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
            Deploy d = null;

            //
            // scan arguments first.
            //
            java.util.ArrayList list = scan_arguments( args );
            if ( list == null ) {
                usage();
                return;
            }
            /* TODO: add an '-a' flag for deploy automation
             * that will give the deploy tool permission
             * to make a best guess on deploying the bean
             * See TODO's about picking the container and resource.
             */

            d = new Deploy( (String)list.get(0) );
            d.deploy( (String)list.get(1) );

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private static void usage() {
        String usage = ""+
                       "OpenEJB Deployer\n"+
                       "\n"+
                       "Usage: Deploy [options] configfile jar-file\n"+
                       "\n"+
                       "configfile - The OpenEJB configuration file\n"+
                       "    for your container system.\n"+
                       "jar-file   - The beans jar file containing the \n"+
                       "    the ejb-jar.xml deployment descriptor.\n"+
                       "\n"+
                       "Example:\n"+
                       "    Deploy myOpenEJB.conf myBeans.jar\n"+
                       "Options:\n"+
                       "-s  -  Generate Stubs\n"+
                       "-t  -  Generate Ties\n"+
                       "-notemp  -  Delete temp dir after generated the stubs and ties.";
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
    }// END interface Bean

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
    }// END EntityBean class

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
    }// END Class SessionBean

    /**
      Scan Arguments
    */
    private static java.util.ArrayList scan_arguments(String[] args){

        if (args.length<1) {
            //usage();
            return null;
        }

        java.util.ArrayList list = new java.util.ArrayList();

        for ( int i = 0;i < args.length; i++ ) {
            if ( args[i].startsWith("-s") ) {
                GENERATE_STUBS = true;
            } else if ( args[i].startsWith("-t") ) {
                GENERATE_TIES = true;
            }else if( args[i].startsWith("-notemp") ){
            	DELETE_TEMP_DIR = true;
            } else {
                list.add(args[i]);
            }
        }

        if ( list.size() != 2 ) {
            //usage();
            return null;
        }

        return list;

    }// END scan_arguments


    /**************************************
              Generate stub & tie
    ***************************************/
    protected void genStubTie(String jarLocation) throws OpenEJBException{

        EjbJar jar = ConfigUtils.readEjbJar(jarLocation);

        Enumeration beanItemList = jar.getEnterpriseBeans().enumerateEnterpriseBeansItem();

	//
	// Copy jarLocation jar to local.
	// After generated stub & tie , local temp jar file will be copy back.
	//
	String TEMP_JAR_FILE0 = TEMP_DIR+ java.io.File.separator +"temp0.jar";
	   
	ConfigUtils.copyZipFile(jarLocation,TEMP_JAR_FILE0);
	    
        while ( beanItemList.hasMoreElements() ) {
            EnterpriseBeansItem item = (EnterpriseBeansItem)beanItemList.nextElement();

            gen_stub( item,jarLocation );

            //
            // archive the class file to ejb jar file.
            //
            saveStubTies(item,TEMP_JAR_FILE0);
        }
        
        ConfigUtils.copyZipFile(TEMP_JAR_FILE0,jarLocation);
        
        if(DELETE_TEMP_DIR)
	    ConfigUtils.deleteTree(TEMP_DIR);
    }

    /** 
     * 1. generate stub & ties
     * 2. compile them
     */
    protected void gen_stub(EnterpriseBeansItem item,String jarLocation)throws OpenEJBException{
        String ejb_name = new String();
        String ejb_home = new String();
        String ejb_remote = new String();

        if ( item.getEntity() == null ) {
            ejb_name = item.getSession().getEjbName();
            ejb_home = item.getSession().getHome();
            ejb_remote = item.getSession().getRemote();
        } else {
            ejb_name = item.getEntity().getEjbName();
            ejb_home = item.getEntity().getHome();
            ejb_remote = item.getEntity().getRemote();
        }    

        try {

            /* must specified ejb.jar;openorb.jar;openorb-tools.jar and openorb-rmi.jar in classpath */
            DeployClassLoader loader = new DeployClassLoader(ClassLoader.getSystemClassLoader());

            loader.addPath(System.getProperty("java.class.path"));
            loader.addPath( jarLocation );

            //
            // Build arguments for JavaToIdl 
            //
            java.util.Vector args_list = new java.util.Vector();
            args_list.add("-d");
            args_list.add(TEMP_DIR);
            args_list.add("-noidl");
            args_list.add("-silence"); 
            if (GENERATE_STUBS) {
                args_list.add("-stub");
            }
            if (this.GENERATE_TIES) {
                args_list.add("-tie");
            }
            String[] home_args=new String[args_list.size()+1];
            String[] remote_args=new String[args_list.size()+1];
            String temp = "";
            int iPos = 0;
            java.util.Iterator it = args_list.iterator();
            for (;it.hasNext();) {
                temp  = (String)it.next();
                home_args[iPos]=temp;
                remote_args[iPos]=temp;
                iPos++;
            }
            home_args[iPos]=ejb_home;
            remote_args[iPos]=ejb_remote;

            //
            // invoke JavaToIdl to generate stubs & ties
            //
            Class clz = loader.loadClass("org.openorb.rmi.compiler.JavaToIdl");
            out.println();
            out.println("-----------------------------------------------------------");
            out.print("Generating stubs and ties for "+ejb_name+"...");
            // == GENERATE EJBObject interface's stub & ties;
            clz.getMethod("main",new Class[]{String[].class}).invoke(null,new Object[]{remote_args});
            // == GENERATE EJBHome interface's stub & ties;
            clz.getMethod("main",new Class[]{String[].class}).invoke(clz,new Object[]{home_args});        
            out.println("done");

            //
            // Compile the source file
            // File format:
            // STUB FILE: _EJBHome_stub.java 
            // TIE  FILE: _EJBHome_tie.java
            //
            out.println();
            out.println("-----------------------------------------------------------");
            out.print("Compiling stub & tie source code for "+ ejb_name +"...");
            org.openejb.alt.config.Compiler compiler = new SunCompiler();
            compiler.setEncoding("UTF8");
            compiler.setClasspath(System.getProperty("java.class.path") + java.io.File.pathSeparator+
                                  jarLocation + java.io.File.pathSeparator+
                                  getStubSrcPath(ejb_home) );
            compiler.setOutputDir( TEMP_DIR );
            compiler.setMsgOutStream(System.out);

            compiler.compile(getStubSrcPath(ejb_home)+"_"+parseName(ejb_home)+"_Stub.java");
            compiler.compile(getStubSrcPath(ejb_home)+"_"+parseName(ejb_home)+"_Tie.java");

            compiler.compile(getStubSrcPath(ejb_remote)+"_"+parseName(ejb_remote)+"_Stub.java");
            compiler.compile(getStubSrcPath(ejb_remote)+"_"+parseName(ejb_remote)+"_Tie.java");
            out.println("done");

        } catch (Exception err) {
            throw new OpenEJBException(err.getMessage(),err);
        }

    }

    // for example: ejb.HelloHome ==> HelloHome
    private String parseName(String ejb_name){
        int pos = 0;
        if ((pos = ejb_name.lastIndexOf("."))>0) {
            return ejb_name.substring(pos+1);
        }
        return ejb_name;
    }

    // for instance: ejb.HelloRemote == > TEMP_DIR\ejb\
    private String getStubSrcPath(String ejb_name){
        StringBuffer temp = new StringBuffer();
        temp.append(TEMP_DIR).append(java.io.File.separator);
        int pos =0;
        if ((pos = ejb_name.lastIndexOf("."))>0) {
            temp.append(ejb_name.substring(0,pos).replace('.',java.io.File.separatorChar));
            temp.append(java.io.File.separator);
        }
        return temp.toString();
    }

    /** save generated stub & tie class file to given jar file. */
    private void saveStubTies(EnterpriseBeansItem item,String jarLocation)throws OpenEJBException{
        
        out.print("Packing the compiled file to jar file...");
        
        String ejb_name = new String();
        String ejb_home = new String();
        String ejb_remote = new String();

        if ( item.getEntity() == null ) {
            ejb_name = item.getSession().getEjbName();
            ejb_home = item.getSession().getHome();
            ejb_remote = item.getSession().getRemote();
        } else {
            ejb_name = item.getEntity().getEjbName();
            ejb_home = item.getEntity().getHome();
            ejb_remote = item.getEntity().getRemote();
        }
        
        /**  
         *  1. Copy dest jar to locale named temp1.jar
         *  2. Can't add class file to temp1.jar directly,so I use temp2.jar for this.
         *  3. Copy temp1.jar contents to temp2.jar and add class file to temp2.jar
         *  4. After finished to generate stubs & ties for EJBHome & EJBObject,temp2.jar will be copy to dest jar.
         */
        String TEMP_JAR_FILE = TEMP_DIR+ java.io.File.separator +"temp.jar";
        
        try {
            //
            // 1. copy EJB jar file to test.jar file 
            //
            ZipHandle srcHdl = ZipTool.open(jarLocation);
            ZipHandle dstHdl = ZipTool.create(TEMP_JAR_FILE);

            ZipTool.copy(srcHdl,dstHdl,null);
            try {

                // == Store package.EJBHome.class to jar file.
                try{
                    ZipTool.insert(getStubSrcPath(ejb_home)+"_"+parseName(ejb_home)+"_Stub.class",toJarPath(ejb_home,"Stub.class"),dstHdl);
                }catch(java.util.zip.ZipException ze){}
                try{
                    ZipTool.insert(getStubSrcPath(ejb_home)+"_"+parseName(ejb_home)+"_Tie.class",toJarPath(ejb_home,"Tie.class"),dstHdl);
                }catch(java.util.zip.ZipException ze){}

                // == Store package.EJBObject.class to jar file.
                try{
                    ZipTool.insert(getStubSrcPath(ejb_remote)+"_"+parseName(ejb_remote)+"_Stub.class",toJarPath(ejb_remote,"Stub.class"),dstHdl);
                }catch(java.util.zip.ZipException ze){}
                try{
                    ZipTool.insert(getStubSrcPath(ejb_remote)+"_"+parseName(ejb_remote)+"_Tie.class",toJarPath(ejb_remote,"Tie.class"),dstHdl);
                }catch(java.util.zip.ZipException ze){}

            
            } catch (Exception err) {
                throw new OpenEJBException(err.getMessage());
            } 
            finally {
                try {
                    ZipTool.close(dstHdl);
                } catch (Exception err) {
                }
                try{
                    ZipTool.close(srcHdl);
                }catch(Exception err){}
            }

            //
            // 2. copy TEMP_JAR_FILE1 to TEMP_JAR_FILE2
            //
            ConfigUtils.copyZipFile(TEMP_JAR_FILE,jarLocation);
            
            /** === Delete TEMP_DIR  === */
            //ConfigUtils.deleteTree(TEMP_DIR);

            out.println("done");

        } catch (Exception err) {
            System.out.println(err);
            throw new OpenEJBException(err.getMessage());
        }

    }

    private static String toJarPath(String ejb_name,String endfix){
        StringBuffer path = new StringBuffer();
        int pos = ejb_name.lastIndexOf(".");
        String pre = new String();
        String _name = new String();

        if(pos>0){
            pre = ejb_name.substring(0,pos);
            pre = pre.replace('.','/'); // ejb.test.HelloHome ==> ejb/test/
            _name = ejb_name.substring(pos+1);
            
            path.append(pre).append("/");
        }
        path.append("_").append(_name).append("_").append(endfix);

        return path.toString();
    }

    private void finishedInfo(){
        out.println("\nCongratulations! Your jar is ready to use with OpenEJB.");

        out.println("\nNOTE: If you move or rename your jar file, you will have to\nupdate the path in this jar's deployment entry in your \nOpenEJB config file.");
    }

}
