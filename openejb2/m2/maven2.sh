#!/bin/sh

src=`pwd`/..
dir=`pwd`/openejb
poms=`pwd`
modules_dir=$dir/modules

echo Removing old version
rm -rf $dir > /dev/null 2>&1

echo Setting up base
mkdir -p $dir > /dev/null 2>&1

cp $poms/ejb-group.pom $dir/pom.xml

echo Setting up modules
mkdir $dir/modules

echo ... core
# TODO: should be ./openejb-core not ./modules/core
cp -r $src/modules/core $modules_dir
cp $poms/ejb-core.pom $modules_dir/core/pom.xml

mv $dir/modules/core/src/test-ejb-jar $modules_dir
cp $poms/ejb-test-jar.pom $modules_dir/test-ejb-jar/pom.xml

( 
  cd $modules_dir/test-ejb-jar
  mkdir -p src/main/java
  mv org src/main/java
  mkdir -p src/main/resources
  mv META-INF src/main/resources
)

# ---------------------------------------------------

find $dir -name 'CVS*' -exec rm -rf {} \; > /dev/null 2>&1

