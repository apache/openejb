Usage instructions
------------------

Requires m2 to be installed. Check out from:
  :pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven-components

If running under windows, requires m2 local repo path not to contain spaces. Refer to http://maven.apache.org/maven2/maven-settings/settings.html on how to change the location of m2 local repo.

Run m2-bootstrap-all.[sh|bat] as per the instructions in that directory.

Create the M2_HOME environment variable, set to the installation directory and add $M2_HOME/bin to the path.

From this directory, run 
  maven2.sh	For Unix
  maven2.bat	For Windows

This will create an openejb subdirectory (sources will be copied from the parent structures into that subdirectory).

From the openejb subdirectory, run:
  m2 install -Dmaven.itest.skip=true

This project depends on the XMLBeans plugin. The current version is at:
  http://cvs.mojo.codehaus.org/mojo/maven-xmlbeans-plugin/

By default, it will be downloaded from the remote repository.

If you need to make changes to the plugin and install an updated version, run from that directory:
  m2 install

