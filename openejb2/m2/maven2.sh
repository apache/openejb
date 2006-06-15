#!/bin/sh

runDir=`pwd`
root=`pwd`/openejb
poms=`pwd`
modules=`pwd`/../modules

echo "Removing old version"
rm -rf $root


echo "Setting up base"
mkdir -p $root
cp $poms/openejb-root.pom $root/pom.xml
cp $poms/openejb-root-profiles.xml $root/profiles.xml
maven -q process-root-pom

echo "Setting up core..."
m1Dir=$modules/core
m2Dir=$root/openejb-core
mkdir -p $m2Dir
cp $poms/openejb-core.pom $m2Dir/pom.xml
{
  mkdir -p $m2Dir/src/main/resources
  cp -r $m1Dir/src/bin $m2Dir/src/main
  #cp -r $m1Dir/src/conf $m2Dir/src/main
  #cp -r $m1Dir/src/etc $m2Dir/src/main
  cp -r $m1Dir/src/etc/META-INF $m2Dir/src/main/resources
  cd $m1Dir/src/java
  find . -name '*.properties' -exec cp {} $m2Dir/src/main/resources --parent \;
  find . -name '*.xml' -exec cp {} $m2Dir/src/main/resources --parent \;
  cd $runDir
  mkdir -p $m2Dir/src/test/java
  cp -r $m1Dir/src/test/* $m2Dir/src/test/java
  mkdir -p $m2Dir/src/test/resources
  cp -r $m1Dir/src/test-resources/* $m2Dir/src/test/resources
  mkdir -p $m2Dir/src/main/java
  cd $m1Dir/src/java
  find . -name '*.java' -exec cp {} $m2Dir/src/main/java --parent \;
  cd $m2Dir
  cp -r ../../unit-tests/openejb-core/* .
  cd $runDir
}


echo "Setting up openejb-builder..."
m1Dir=$modules/openejb-builder
m2Dir=$root/openejb-builder
mkdir -p $m2Dir
cp $poms/openejb-builder.pom $m2Dir/pom.xml
{
  mkdir -p $m2Dir/src/test/java
  cp -r $m1Dir/src/test/* $m2Dir/src/test/java
  mkdir -p $m2Dir/src/test/resources
  cp -r $m1Dir/src/test-resources/* $m2Dir/src/test/resources
  cp -r $m1Dir/src/test-cmp $m2Dir/src/test
  mkdir -p $m2Dir/src/main
  cp -r $m1Dir/src/schema $m2Dir/src/main
  mkdir -p $m2Dir/src/main/java
  cd $m1Dir/src/java
  find . -name '*.java' -exec cp {} $m2Dir/src/main/java --parent \;
  cd $m2Dir
  cp -r ../../unit-tests/openejb-builder/* .
  cd $runDir
}

echo "Setting up pkgen-builder..."
m1Dir=$modules/pkgen-builder
m2Dir=$root/pkgen-builder
mkdir -p $m2Dir
cp $poms/pkgen-builder.pom $m2Dir/pom.xml
{
  mkdir -p $m2Dir/src/main
  cp -r $m1Dir/src/java $m2Dir/src/main
  cp -r $m1Dir/src/schema $m2Dir/src/main
}


echo "Setting up openejb-webadmin..."
m1Dir=$modules/webadmin
m2Dir=$root/openejb-webadmin
mkdir -p $m2Dir
cp $poms/openejb-webadmin.pom $m2Dir/pom.xml


echo "Setting up openejb-webadmin-commons..."
m2Dir=$root/openejb-webadmin/openejb-webadmin-commons
mkdir -p $m2Dir
cp $poms/openejb-webadmin-commons.pom $m2Dir/pom.xml
{
  mkdir -p $m2Dir/src/main/java/org/openejb/webadmin
  cp $m1Dir/src/java/org/openejb/webadmin/*.java $m2Dir/src/main/java/org/openejb/webadmin
}


echo "Setting up openejb-webadmin-clienttools..."
m2Dir=$root/openejb-webadmin/openejb-webadmin-clienttools
mkdir -p $m2Dir
cp $poms/openejb-webadmin-clienttools.pom $m2Dir/pom.xml
{
  mkdir -p $m2Dir/src/main/java/org/openejb/webadmin/clienttools
  cp $m1Dir/src/java/org/openejb/webadmin/clienttools/*.java $m2Dir/src/main/java/org/openejb/webadmin/clienttools
  mkdir -p $m2Dir/src/main/resources/META-INF
  cp $m1Dir/src/java/org/openejb/webadmin/clienttools/*.xml  $m2Dir/src/main/resources/META-INF
}


echo "Setting up openejb-webadmin-ejbgen..."
m2Dir=$root/openejb-webadmin/openejb-webadmin-ejbgen
mkdir -p $m2Dir
cp $poms/openejb-webadmin-ejbgen.pom $m2Dir/pom.xml
{
  mkdir -p $m2Dir/src/main/java/org/openejb/webadmin/ejbgen
  cp $m1Dir/src/java/org/openejb/webadmin/ejbgen/*.java $m2Dir/src/main/java/org/openejb/webadmin/ejbgen
  mkdir -p $m2Dir/src/main/resources/META-INF
  cp $m1Dir/src/java/org/openejb/webadmin/ejbgen/*.xml  $m2Dir/src/main/resources/META-INF
}


echo "Setting up openejb-webadmin-main..."
m2Dir=$root/openejb-webadmin/openejb-webadmin-main
mkdir -p $m2Dir
cp $poms/openejb-webadmin-main.pom $m2Dir/pom.xml
{
  mkdir -p $m2Dir/src/main/java/org/openejb/webadmin/main
  cp $m1Dir/src/java/org/openejb/webadmin/main/*.java $m2Dir/src/main/java/org/openejb/webadmin/main
  mkdir -p $m2Dir/src/main/java/org/openejb/webadmin/httpd
  cp $m1Dir/src/java/org/openejb/webadmin/httpd/*.* $m2Dir/src/main/java/org/openejb/webadmin/httpd
  mkdir -p $m2Dir/src/main/resources/META-INF
  cp $m1Dir/src/java/org/openejb/webadmin/main/*.xml  $m2Dir/src/main/resources/META-INF
}

echo "Setting up test-ear..."
m2Dir=$root/test-ear
m1Dir=$modules/openejb-builder/src/test-ear
{
  mkdir -p $m2Dir/src/main/resources/META-INF
  cp -r $m1Dir/META-INF/geronimo-application.xml $m2Dir/src/main/resources/META-INF
  cp $poms/test-ear.pom $m2Dir/pom.xml
}

echo "Setting up test-ant-ear..."
m2Dir=$root/test-ant-ear
m1Dir=$modules/openejb-builder/src/test-ant
{
  mkdir -p $m2Dir/src/main/resources/META-INF
  cp $m1Dir/META-INF/geronimo-application.xml $m2Dir/src/main/resources/META-INF
  cp $poms/test-ant-ear.pom $m2Dir/pom.xml

  # This is what the ant tests want - could be improved
  cp -r $m1Dir $root/openejb-builder/src
}

echo "Setting up test-ejb-jar..."
m2Dir=$root/test-ejb-jar
m1Dir=$modules/openejb-builder/src/test-ejb-jar
{
  mkdir -p $m2Dir/src/main/resources
  cp -r $m1Dir/META-INF $m2Dir/src/main/resources
  mkdir -p $m2Dir/src/main/java
  cp -r $m1Dir/org $m2Dir/src/main/java
  cp $poms/test-ejb-jar.pom $m2Dir/pom.xml

  # This is what the ant tests want - could be improved
  cp -r $m1Dir $root/openejb-builder/src
}

echo "Cleaning up copied CVS folders"
cd openejb
find . -name 'CVS' -type d -exec rm -rf {} \;
cd ..
