--------------------------------------------------------------------------------
  Build and installation instructions for the Apache OpenEJB Eclipse Toolset
--------------------------------------------------------------------------------

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

--------------------------------------------------------------------------------
  Contents
--------------------------------------------------------------------------------
1. Building from source
2. Installation
3. Adding a server and deploying EJBs
4. Generating EJB 3.0 annotations from ejb-jar.xml (still under development)
5. Generating annotations from the command line (TBD)

--------------------------------------------------------------------------------
  1. Building from source
--------------------------------------------------------------------------------

To build the plugins from source you will require the following:

	- Java SDK 5.0+ (not yet tested on Java 6)
	- Apache Maven 2 (tested with Maven 2.0.7)

Steps to build:

1. Make sure that your JAVA_HOME environment variable is set to the path to your
   Java SDK.

2. Run 'mvn clean install' at a command prompt.

This will cause *lots* of things to be downloaded, including the Eclipse SDK.
You will need to be pretty patient first time around. The whole process took
25 mins with a clean Maven repository and a 4Mbps connection to the Internet.
Subsequent builds will be much quicker, as Maven will not need to download the
dependencies again.

Steps to create an Eclipse project for the source:

1. Create a workspace.

2. Run 'mvn -Declipse.workspace=/path/to/eclipse/workspace eclipse:add-maven-repo'
   This adds the M2_REPO classpath varible to the Eclipse workspace, allowing
   projects to reference jars in the Maven repository.

3. Run 'mvn eclipse:clean eclipse:eclipse'
   (add -DdownloadSources=true if you want sources for dependencies downloaded
    and linked in as well)

4. Open the workspace with Eclipse, and use the import wizard to 'Import 
   existing projects into workspace'. Navigate to the folder where you have
   checked out the source. Eclipse should find the projects created.

--------------------------------------------------------------------------------
  2. Installation
--------------------------------------------------------------------------------

1. Click on Help -> Software Updates -> Find and install
2. Select Search for new features to install
3. Add a new archived site
4. Browse to update-site zip under assembly/target
5. Click Finish
6. Select the OpenEJB feature
7. Accept license agreement
8. Check the installation directory
9. Click Finish

--------------------------------------------------------------------------------
  3. Adding a server and deploying EJBs
--------------------------------------------------------------------------------

Adding a new installed runtime

1. Click on Window > Preferences
2. In the Preferences window, navigate to Server > Installed Runtimes
3. On the Installed Runtimes page, click on Add, then choose Apache > OpenEJB 3.0.0
4. Click next and specify the location of the installation directory of OpenEJB
5. Click finish

Creating a new server

1. Open the J2EE perspective
2. go to the servers view
3. Right-click anywhere on the servers view and select New > Server (in the 
	context menu)
4. Select OpenEJB from the list

Starting and stopping the server

1. Servers view should now have the OpenEJB server instance
2. You can right-click on it and choose start / Stop. (You can also use the 
	toolbar on the servers view to do the same)

Deploying an EJB

1. Create an EJB project
2. Create an EJB
3. Once you are finished compiling the EJB, drag the EJB project on top of the 
	server in the Servers view
4. Start the server
5. Your EJB is now deployed to the server

--------------------------------------------------------------------------------
  4. Generating EJB 3.0 annotations from ejb-jar.xml (still under development)
--------------------------------------------------------------------------------

1. Ensure you have the EJB 3.0 API jar on the build path of your project

2. Right click on the ejb-jar.xml in your project, and select 
	OpenEJB -> Generate annotations
	
3. You should see a refacting preview, showing the annotations that will be
	added to your source.

--------------------------------------------------------------------------------
  5. Generating annotations from the command line (TBD)
--------------------------------------------------------------------------------



