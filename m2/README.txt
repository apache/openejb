Usage instructions
------------------

Requires m2 to be installed. Check out from:
  :pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven-components

Run m2-bootstrap-all.[sh|bat] as per the instructions in that directory.

Create the M2_HOME environment variable, set to the installation directory and add $M2_HOME/bin to the path.

From this directory, run 
  maven2.sh

This will create an openejb subdirectory (sources will be copied from the parent structures into that subdirectory).

From the openejb subdirectory, run:
  m2 install

