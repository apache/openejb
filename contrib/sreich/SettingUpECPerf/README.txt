Setting up ECperf with OpenEJB

Download ECPerf from http://developer.java.sun.com/developer/releases/j2ee/ecperf/

unpack it. Unpack the modifications in the directory containg the ecperf base directory. The additions include
* the directory called ECPerfBMPServer, which contains the stub files and configuration files for the OpenEJB container to deploy the EJBs
* src/deploy/woejb{bmp,cmp} deployment descriptor that package the EJBs either for the BMP or the CMP test
* a modified shell script to start the driver program. You need to modify the location of OpenORB and the RMI layer at the end of the script.

In order to set up the test, you need to read carefully through the Readme and Release Notes. Whenever invoke ant, make sure you include -Dappserver=woejbbmp in the command line.

The default port that Tomcat uses to contact the EJB tier is 7890, meaning you name service should bind to that port, or you have to modify the tomcat setup.

Download Tomcat 4.0.1 and unpack it. Change directory to jakarta-tomcat-4.0.1 and unpack the tomcat patch file. Modify the locations of the required jar files in bin/catalina.sh

Change into the webapps directory. Unjar $(ECPERF_HOME)/jars/Emulator.war and $(ECPERF_HOME)/jars/Supplier.war into this directory. If you don't unpack them, it won't work.

Great! Now let's launch the EJB server. I don't include any details here since the way the container is launched differs greatly from the way the WebObjects is launched. 

Start Tomcat with "bin/catalina.sh start", then go to "http://localhost:8080/petstore". 

Enjoy!