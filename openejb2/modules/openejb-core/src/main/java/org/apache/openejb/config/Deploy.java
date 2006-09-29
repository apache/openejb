/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ejb11.*;
import org.apache.openejb.config.sys.Connector;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.util.JarUtils;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

/**
 * This class represents a command line tool for deploying beans.
 * <p/>
 * At the moment it contains multiple println statements
 * and statements that read input from the user.
 * <p/>
 * These statements are really in chunks in specific times throughout
 * the class.  These chunks could be refactored into methods. Then
 * the implementation of those methods could actually be delegated
 * to another class that implements a specific interface we create.
 * <p/>
 * The command line statements could be moved into an implementation
 * of this new interface. We could then create another implementation
 * that gathers information from a GUI.
 * <p/>
 * This would give us a Deploy API rather than just a command line
 * tool.  Then beans could be deployed programmatically by another
 * application, by a GUI screen, or by command line.
 * <p/>
 * Note: The command line version should be finished first!!!  We
 * don't want to start on a crusade of abstracting code that doesn't
 * yet exist.  Functionality first, neat flexible stuff later.
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class Deploy {

    static protected Messages _messages = new Messages("org.apache.openejb.alt.util.resources");

    private final String DEPLOYMENT_ID_HELP =
            "\nDeployment ID ----- \n\nA name for the ejb that is unique not only in this jar, but \nin all the jars in the container system.  This name will \nallow OpenEJB to place the bean in a global index and \nreference the bean quickly.  OpenEJB will also use this name \nas the global JNDI name for the Remote Server and the Local \nServer.  Clients of the Remote or Local servers can use this\nname to perform JNDI lookups.\n\nThe other EJB Server's using OpenEJB as the EJB Container \nSystem may also use this name to as part of a global JNDI \nnamespace available to remote application clients.\n\nExample: /my/acme/bugsBunnyBean\n\nSee http://openejb.sf.net/deploymentids.html for details.\n";
    private final String CONTAINER_ID_HELP =
            "\nContainer ID ----- \n\nThe name of the container where this ejb should run. \nContainers are declared and configured in the openejb.conf\nfile.\n";
    private final String CONNECTOR_ID_HELP =
            "\nConnector ID ----- \n\nThe name of the connector or JDBC resource this resoure \nreference should be mapped to. Connectors and JDBC resources \nare declared and configured in the openejb.conf file.\n";

    /*=======----------TODO----------=======
      Neat options that this Deploy tool 
      could support
     
      Contributions and ideas welcome!!!
     =======----------TODO----------=======*/

    /**
     * Idea for a command line option
     * <p/>
     * If there is only one container of the appropriate type
     * for a bean then the bean is automatically assigned to that
     * container.  The user is notified unless the QUIET flag is true.
     * <p/>
     * not implemented
     */
    private boolean AUTO_ASSIGN;

    /**
     * Idea for a command line option
     * <p/>
     * -m   Move the jar to the OPENEJB_HOME/beans directory
     * <p/>
     * not implemented
     */
    private boolean MOVE_JAR;

    /**
     * Idea for a command line option
     * <p/>
     * -f   Force an overwrite if the jar already exists
     * <p/>
     * not implemented
     */
    private boolean FORCE_OVERWRITE_JAR;

    /**
     * Idea for a command line option
     * <p/>
     * -c   Copy the jar to the OPENEJB_HOME/beans directory
     * <p/>
     * not implemented
     */
    private boolean COPY_JAR;

    /**
     * Idea for a command line option
     * <p/>
     * Will automatically create an OpenEJB configuration
     * file that can accomodate the beans in the jar.
     * <p/>
     * If there already is a config file, but, for example, there
     * is not a container that is compatable for a bean type in the
     * jar, then a useable container of the right type will be
     * automatically created with default values.
     * <p/>
     * not implemented
     */
    private boolean AUTO_CONFIG;

    /**
     * Idea for a command line option
     * <p/>
     * Will generate the bean's deployment id from a particular id generation
     * strategy.
     * <p/>
     * -g[S#]
     * <p/>
     * S# can be a number key to a generation strategy that is
     * looked up internally.
     * <p/>
     * ----------------------------
     * One strategy could be:
     * id = jar_directory + ejb-name
     * example:
     * DIR   path/to/a/jarfile/myBeans.jar
     * BEAN  CustomerBean
     * ID    path/to/a/jarfile/CustomerBean
     * <p/>
     * ----------------------------
     * Another strategy:
     * Just use the ejb-name
     * DIR   doesnt/matter/path/to/a/jarfile/myBeans.jar
     * BEAN  CustomerBean
     * ID    CustomerBean
     * <p/>
     * If ejb-name already looked like a JNDI name
     * then this would work great, otherwise there
     * would be a high chance of name collitions in
     * the OpenEJB IntraVM global namespace.
     * ----------------------------
     * <p/>
     * not implemented
     */
    private boolean GENERATE_DEPLOYMENT_ID;

    /**
     * Idea for a command line option
     * <p/>
     * Generate the CORBA stubs and ties
     * and add them to the jar so people don't
     * have to run a seperate tool to do that.
     * <p/>
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

    public void init(String openejbConfigFile) throws OpenEJBException {
        try {
            if (System.getProperty("openejb.nobanner") == null) {
                printVersion();
                System.out.println("");
            }

            in = new DataInputStream(System.in);
            out = System.out;

            configFile = openejbConfigFile;
            if (configFile == null) {
                try {
                    configFile = System.getProperty("openejb.configuration");
                } catch (Exception e) {
                }
            }
            if (configFile == null) {
                configFile = ConfigUtils.searchForConfiguration();
            }
            config = ConfigUtils.readConfig(configFile);

            /* Load container list */
            containers = config.getContainer();

            /* Load resource list */
            resources = config.getConnector();

        } catch (Exception e) {
            // TODO: Better exception handling.
            e.printStackTrace();
            throw new OpenEJBException(e.getMessage());
        }

    }

    /*------------------------------------------------------*/
    /*    Methods for starting the deployment process       */
    /*------------------------------------------------------*/

    private void deploy(String jarLocation) throws OpenEJBException {
        EjbValidator validator = new EjbValidator();

        EjbSet set = validator.validateJar(jarLocation);

        if (set.hasErrors() || set.hasFailures()) {
            validator.printResults(set);
            System.out.println();
            System.out.println("Jar not deployable.");
            System.out.println();
            System.out.println("Use the validator with -vvv option for more details.");
            System.out.println("See http://openejb.sf.net/validate.html for usage.");
            return;
        }
        EjbJar jar = set.getEjbJar();

        OpenejbJar openejbJar = new OpenejbJar();

        Bean[] beans = getBeans(jar);

        listBeanNames(beans);

        for (int i = 0; i < beans.length; i++) {
            openejbJar.addEjbDeployment(deployBean(beans[i], jarLocation));
        }

        if (MOVE_JAR) {
            jarLocation = moveJar(jarLocation);
        } else if (COPY_JAR) {
            jarLocation = copyJar(jarLocation);
        }

        /* TODO: Automatically updating the users
        config file might not be desireable for
        some people.  We could make this a 
        configurable option. 
        */
        addDeploymentEntryToConfig(jarLocation);

        saveChanges(jarLocation, openejbJar);

    }

    private EjbDeployment deployBean(Bean bean, String jarLocation) throws OpenEJBException {
        EjbDeployment deployment = new EjbDeployment();
        Class tempBean = SafeToolkit.loadTempClass(bean.getHome(), jarLocation);

        out.println("\n-----------------------------------------------------------");
        out.println("Deploying bean: " + bean.getEjbName());
        out.println("-----------------------------------------------------------");
        deployment.setEjbName(bean.getEjbName());

        if (GENERATE_DEPLOYMENT_ID) {
            deployment.setDeploymentId(autoAssignDeploymentId(bean));
        } else {
            deployment.setDeploymentId(promptForDeploymentId());
        }

        if (AUTO_ASSIGN) {
            deployment.setContainerId(autoAssignContainerId(bean));
        } else {
            deployment.setContainerId(promptForContainerId(bean));
        }

        ResourceRef[] refs = bean.getResourceRef();
        if (refs.length > 0) {
            out.println("\n==--- Step 3 ---==");
            out.println("\nThis bean contains the following references to external \nresources:");

            out.println("\nName\t\t\tType\n");

            for (int i = 0; i < refs.length; i++) {
                out.print(refs[i].getResRefName() + "\t");
                out.println(refs[i].getResType());
            }

            out.println("\nThese references must be linked to the available resources\ndeclared in your config file.");

            out.println("Available resources are:");
            listResources(resources);
            for (int i = 0; i < refs.length; i++) {
                deployment.addResourceLink(resolveResourceRef(refs[i]));
            }
        }

        //check for OQL statement
        if (bean.getType().equals("CMP_ENTITY")) {
            promptForOQLForEntityBeans(tempBean, deployment);
        }

        return deployment;
    }

    private void promptForOQLForEntityBeans(Class bean, EjbDeployment deployment)
            throws OpenEJBException {
        org.apache.openejb.config.ejb11.Query query;
        QueryMethod queryMethod;
        MethodParams methodParams;
        boolean instructionsPrinted = false;

        Method[] methods = bean.getMethods();
        Class[] parameterList;
        Class[] exceptionList;
        String answer = null;
        String parameters;

        StringTokenizer parameterTokens;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find")
                    && !methods[i].getName().equals("findByPrimaryKey")) {

                if (!instructionsPrinted) {
                    out.println("\n==--- Step 4 ---==");
                    out.println("\nThis part of the application allows you to add OQL (Object\n"
                            + "Query Language) statements to your CMP Entity find methods.\n"
                            + "Below is a list of find methods, each of which should get an \n"
                            + "OQL statement.\n"
                            + "\n"
                            + "OQL statements are very similar to SQL statments with the \n"
                            + "exception that they reference class names rather than table\n"
                            + "names.  Find method parameters can be referenced in OQL \n"
                            + "statements as $1, $2, $3, and so on.  \n"
                            + "\n"
                            + "If you had a find method in your home interface like this one:\n"
                            + "\n"
                            + "  public Employee findByLastName( String lName )\n"
                            + "\n"
                            + "Then you could use an OQL method like the following:\n"
                            + "\n"
                            + "  SELECT o FROM org.acme.employee.EmployeeBean o WHERE o.lastname = $1\n"
                            + "\n"
                            + "In this example, the $1 is referring to the first parameter, \n"
                            + "which is the String lName.\n"
                            + "\n"
                            + "For more information on OQL see:\n"
                            + "http://www.openejb.org/cmp_guide.html\n");

                    instructionsPrinted = true;
                }

                out.print("Method: ");
                parameterList = methods[i].getParameterTypes();

                // and it's parameters
                out.print(methods[i].getName() + "(");
                for (int j = 0; j < parameterList.length; j++) {
                    out.print(parsePartialClassName(parameterList[j].getName()));
                    if (j != (parameterList.length - 1)) {
                        out.print(", ");
                    }
                }
                out.println(") ");

                try {
                    boolean replied = false;

                    while (!replied) {
                        out.println("Please enter your OQL Statement here.");
                        out.print("\nOQL Statement: ");
                        answer = in.readLine();
                        if (answer.length() > 0) {
                            replied = true;
                        }
                    }

                } catch (Exception e) {
                    throw new OpenEJBException(e.getMessage());
                }

                //create a new query and add it to the deployment
                if (answer != null && !answer.equals("")) {
                    query = new org.apache.openejb.config.ejb11.Query();
                    methodParams = new MethodParams();
                    queryMethod = new QueryMethod();

                    //loop through the list of parameters
                    for (int j = 0; j < parameterList.length; j++) {
                        methodParams.addMethodParam(parameterList[j].getName());
                    }

                    queryMethod.setMethodParams(methodParams);
                    queryMethod.setMethodName(methods[i].getName());
                    query.setQueryMethod(queryMethod);
                    query.setObjectQl(answer);

                    deployment.addQuery(query);

                    out.println("\nYour OQL statement was successfully added to the jar.\n");
                }
            }
        }
    }

    private String parsePartialClassName(String className) {
        if (className.indexOf('.') < 1) return className;
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /*------------------------------------------------------*/
    /*    Methods for deployment id mapping                 */
    /*------------------------------------------------------*/
    private void listBeanNames(Bean[] beans) {
        out.println("This jar contains the following beans:");
        for (int i = 0; i < beans.length; i++) {
            out.println("  " + beans[i].getEjbName());
        }
        out.println();
    }

    private String promptForDeploymentId() throws OpenEJBException {
        String answer = null;
        try {
            boolean replied = false;
            out.println("\n==--- Step 1 ---==");
            out.println("\nPlease specify a deployment id for this bean.");

            while (!replied) {
                out.println("Type the id or -help for more information.");
                out.print("\nDeployment ID: ");
                answer = in.readLine();
                if ("-help".equals(answer)) {
                    out.println(DEPLOYMENT_ID_HELP);
                } else if (answer.length() > 0) {
                    replied = true;
                }
            }
        } catch (Exception e) {
            throw new OpenEJBException(e.getMessage());
        }
        return answer;
    }

    private String autoAssignDeploymentId(Bean bean) throws OpenEJBException {
        String answer = bean.getEjbName();
        out.println("\n==--- Step 1 ---==");
        out.println("\nAuto assigning the ejb-name as the deployment id for this bean.");
        out.print("\nDeployment ID: " + answer);

        return answer;
    }

    /*------------------------------------------------------*/
    /*    Methods for container mapping                     */
    /*------------------------------------------------------*/

    private String promptForContainerId(Bean bean) throws OpenEJBException {
        String answer = null;
        boolean replied = false;
        out.println("\n==--- Step 2 ---==");
        out.println("\nPlease specify which container the bean will run in.");
        out.println("Available containers are:");

        Container[] cs = getUsableContainers(bean);

        if (cs.length == 0) {
            /* TODO: Allow or Automatically create a useable container
             * Stopping the deployment process because there is no
             * container of the right bean type is a terrible way
             * deal with the problem.  Instead, we should either 
             * 1) Automatically create a container for them and notify them
             *    that we have done so.
             * 2) Allow them to create their own container.
             * 3) Some combination of 1 and 2.
             */
            out.println("!! There are no "
                    + bean.getType()
                    + " containers declared in "
                    + configFile
                    + " !!");
            out.println("A "
                    + bean.getType()
                    + " container must be declared and \nconfigured in your configuration file before this jar can\nbe deployed.");
            System.exit(-1);
        } else if (cs.length == 0) {
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

            while (!replied) {
                out.println("\nType the number of the container\n-options to view the list again\nor -help for more information.");
                out.print("\nContainer: ");
                answer = in.readLine();
                if ("-help".equals(answer)) {
                    out.println(CONTAINER_ID_HELP);
                } else if ("-options".equals(answer)) {
                    listContainers(cs);
                } else if (answer.length() > 0) {
                    try {
                        choice = Integer.parseInt(answer);
                    } catch (NumberFormatException nfe) {
                        out.println("\'" + answer + "\' is not a numer.");
                        continue;
                    }
                    if (choice > cs.length || choice < 1) {
                        out.println(choice + " is not an option.");
                        continue;
                    }
                    replied = true;
                }
            }
        } catch (Exception e) {
            throw new OpenEJBException(e.getMessage());
        }
        return cs[choice - 1].getId();
    }

    private String autoAssignContainerId(Bean bean) throws OpenEJBException {
        String answer = null;
        boolean replied = false;
        out.println("\n==--- Step 2 ---==");
        out.println("\nAuto assigning the container the bean will run in.");

        Container[] cs = getUsableContainers(bean);

        if (cs.length == 0) {
            /* TODO: Allow or Automatically create a useable container
             * Stopping the deployment process because there is no
             * container of the right bean type is a terrible way
             * deal with the problem.  Instead, we should either 
             * 1) Automatically create a container for them and notify them
             *    that we have done so.
             * 2) Allow them to create their own container.
             * 3) Some combination of 1 and 2.
             */
            out.println("!! There are no "
                    + bean.getType()
                    + " containers declared in "
                    + configFile
                    + " !!");
            out.println("A "
                    + bean.getType()
                    + " container must be declared and \nconfigured in your configuration file before this jar can\nbe deployed.");
            System.exit(-1);
        }

        out.println("\nContainer: " + cs[0].getId());
        return cs[0].getId();
    }

    private void listContainers(Container[] containers) {
        out.println("\nNum \tType     \tID\n");

        for (int i = 0; i < containers.length; i++) {
            out.print((i + 1) + "\t");
            out.print(containers[i].getCtype() + "\t");
            out.println(containers[i].getId());
        }
    }

    /*------------------------------------------------------*/
    /*    Methods for connection(resource) mapping          */
    /*------------------------------------------------------*/
    private ResourceLink resolveResourceRef(ResourceRef ref) throws OpenEJBException {
        String answer = null;
        boolean replied = false;

        out.println("\nPlease link reference: " + ref.getResRefName());

        if (resources.length == 0) {
            /* TODO: 1, 2 or 3
             * 1) Automatically create a connector and link the reference to it.
             * 2) Something more creative
             * 3) Some ultra flexible combination of 1 and 2.
             */
            out.println("!! There are no resources declared in " + configFile + " !!");
            out.println("A resource connector must be declared and configured in \nyour configuration file before this jar can be deployed.");
            System.exit(-2);
        } else if (resources.length == 0) {
            /* TODO: 1, 2 or 3
             * 1) Automatically link the reference to the connector
             * 2) Something more creative
             * 3) Some ultra flexible combination of 1 and 2.
             */
        }

        int choice = 0;
        try {
            while (!replied) {
                out.println("\nType the number of the resource to link the bean's \nreference to, -options to view the list again, or -help\nfor more information.");
                out.print("\nResource: ");
                answer = in.readLine();
                if ("-help".equals(answer)) {
                    out.println(CONNECTOR_ID_HELP);
                } else if ("-options".equals(answer)) {
                    listResources(resources);
                } else if (answer.length() > 0) {
                    try {
                        choice = Integer.parseInt(answer);
                    } catch (NumberFormatException nfe) {
                        out.println("\'" + answer + "\' is not a number.");
                        continue;
                    }
                    if (choice > resources.length || choice < 1) {
                        out.println(choice + " is not an option.");
                        continue;
                    }
                    replied = true;
                }
            }
        } catch (Exception e) {
            throw new OpenEJBException(e.getMessage());
        }

        ResourceLink link = new ResourceLink();
        link.setResRefName(ref.getResRefName());
        link.setResId(resources[choice - 1].getId());
        return link;
    }

    private void listResources(Connector[] connectors) {
        out.println("\nNum \tID\n");

        for (int i = 0; i < connectors.length; i++) {
            out.print((i + 1) + "\t");
            out.println(connectors[i].getId());
        }
    }

    private void saveChanges(String jarFile, OpenejbJar openejbJar) throws OpenEJBException {
        out.println("\n-----------------------------------------------------------");
        out.println("Done collecting deployment information!");

        out.print("Creating the openejb-jar.xml file...");
        ConfigUtils.writeOpenejbJar("META-INF/openejb-jar.xml", openejbJar);

        out.println("done");

        out.print("Writing openejb-jar.xml to the jar...");
        JarUtils.addFileToJar(jarFile, "META-INF/openejb-jar.xml");

        out.println("done");

        if (configChanged) {
            out.print("Updating your system config...");
            ConfigUtils.writeConfig(configFile, config);

            out.println("done");
        }

        out.println("\nCongratulations! Your jar is ready to use with OpenEJB.");
        out.println("\nIf the OpenEJB remote server is already running, you will\nneed to restart it in order for OpenEJB to recognize your bean.");
        out.println("\nNOTE: If you move or rename your jar file, you will have to\nupdate the path in this jar's deployment entry in your \nOpenEJB config file.");

    }

    /*------------------------------------------------------*/
    /*    Methods for exception handling                    */
    /*------------------------------------------------------*/
    private void logException(String m) throws OpenEJBException {
        System.out.println("[OpenEJB] " + m);
        throw new OpenEJBException(m);
    }

    private void logException(String m, Exception e) throws OpenEJBException {
        m += " : " + e.getMessage();
        System.out.println("[OpenEJB] " + m);
        //e.printStackTrace();
        throw new OpenEJBException(m);
    }

    /*------------------------------------------------------*/
    /*    Static methods                                    */
    /*------------------------------------------------------*/

    public static void main(String args[]) {
        try {
            org.apache.openejb.util.ClasspathUtils.addJarsToPath("lib");
            org.apache.openejb.util.ClasspathUtils.addJarsToPath("dist");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Deploy d = new Deploy();

            if (args.length == 0) {
                printHelp();
                return;
            }

            for (int i = 0; i < args.length; i++) {
                //AUTODEPLOY
                if (args[i].equals("-a")) {
                    d.AUTO_ASSIGN = true;
                    d.GENERATE_DEPLOYMENT_ID = true;
                } else if (args[i].equals("-m")) {
                    d.MOVE_JAR = true;
                } else if (args[i].equals("-f")) {
                    d.FORCE_OVERWRITE_JAR = true;
                } else if (args[i].equals("-c")) {
                    d.COPY_JAR = true;
                } else if (args[i].equals("-C")) {
                    d.AUTO_ASSIGN = true;
                } else if (args[i].equals("-D")) {
                    d.GENERATE_DEPLOYMENT_ID = true;
                } else if (args[i].equals("-conf")) {
                    if (args.length > i + 1) {
                        System.setProperty("openejb.configuration", args[++i]);
                    }
                } else if (args[i].equals("-l")) {
                    if (args.length > i + 1) {
                        System.setProperty("log4j.configuration", args[++i]);
                    }
                } else if (args[i].equals("-d")) {
                    if (args.length > i + 1) {
                        System.setProperty("openejb.home", args[++i]);
                    }
                } else if (args[i].equals("-help")) {
                    printHelp();
                } else if (args[i].equals("-examples")) {
                    printExamples();
                } else if (args[i].equals("-version")) {
                    printVersion();
                } else {
                    // We must have reached the jar list
                    d.init(null);
                    for (; i < args.length; i++) {
                        try {
                            d.deploy(args[i]);
                        } catch (Exception e) {
                            System.out.print("\nERROR in ");
                            System.out.println(args[i]);
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    private static void printVersion() {
        /*
         * Output startup message
         */
        Properties versionInfo = new Properties();

        try {
            JarUtils.setHandlerSystemProperty();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
        } catch (java.io.IOException e) {
        }

        System.out.println("OpenEJB Deploy Tool "
                + versionInfo.get("version")
                + "    build: "
                + versionInfo.get("date")
                + "-"
                + versionInfo.get("time"));
        System.out.println("" + versionInfo.get("url"));
    }

    private static void printHelp() {
        String header = "OpenEJB Deploy Tool ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            header += versionInfo.get("version");
        } catch (java.io.IOException e) {
        }

        System.out.println(header);

        // Internationalize this
        try {
            InputStream in =
                    new URL("resource:/openejb/deploy.txt").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write(b);
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    private static void printExamples() {
        String header = "OpenEJB Deploy Tool ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            header += versionInfo.get("version");
        } catch (java.io.IOException e) {
        }

        System.out.println(header);

        // Internationalize this
        try {
            InputStream in =
                    new URL("resource:/openejb/deploy-examples.txt").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write(b);
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    /*------------------------------------------------------*/
    /*    Refactored Methods                                */
    /*------------------------------------------------------*/
    private Bean[] getBeans(EjbJar jar) {
        return EjbJarUtils.getBeans(jar);
    }

    private String moveJar(String jar) throws OpenEJBException {
        return EjbJarUtils.moveJar(jar, FORCE_OVERWRITE_JAR);
    }

    private String copyJar(String jar) throws OpenEJBException {
        return EjbJarUtils.copyJar(jar, FORCE_OVERWRITE_JAR);
    }

    private Container[] getUsableContainers(Bean bean) {
        return EjbJarUtils.getUsableContainers(containers, bean);
    }

    private void addDeploymentEntryToConfig(String jarLocation) {
        configChanged = ConfigUtils.addDeploymentEntryToConfig(jarLocation, config);
    }
}
