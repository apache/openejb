#!/bin/sh

src=`pwd`/..
dir=`pwd`/openejb
poms=`pwd`
modules_dir=$dir/modules

echo "Backing up IntelliJ files"
mkdir -p intellij
find openejb -name '*.iml' -exec cp --parents {} intellij \;
cp openejb/*.iws intellij/openejb
cp openejb/*.ipr intellij/openejb

echo "Removing old version"
rm -rf $dir

echo "Setting up base"
mkdir -p $dir

cp $poms/ejb-group.pom $dir/pom.xml

echo "Setting up modules"
mkdir -p $modules_dir

echo "... core"
# TODO: should be ./openejb-core not ./modules/core
cp -r $src/modules/core $modules_dir
cp $poms/ejb-core.pom $modules_dir/core/pom.xml
(
  cd $modules_dir/core
  mkdir -p src/main
  mkdir -p src/test/java
  mv src/conf src/main/resources
  mv src/test/org src/test/java
  mv src/test-resources src/test/resources
  mv src/java src/main
)

echo "... test-ejb-jar"
mv $dir/modules/core/src/test-ejb-jar $modules_dir
cp $poms/ejb-test-jar.pom $modules_dir/test-ejb-jar/pom.xml

( 
  cd $modules_dir/test-ejb-jar
  mkdir -p src/main/java
  mv org src/main/java
  mkdir -p src/main/resources
  mv META-INF src/main/resources
)

echo "... builder"
cp -r $src/modules/openejb-builder $modules_dir
cp $poms/ejb-builder.pom $modules_dir/openejb-builder/pom.xml
rm -rf $modules_dir/openejb-builder/src/test-ejb-jar
(
  cd $modules_dir/openejb-builder
  mkdir -p src/main
  mkdir -p src/test/java
  mv src/java src/main
  mv src/test/org src/test/java
  mv src/test-resources src/test/resources
)

echo "... test-ear"
mv $dir/modules/openejb-builder/src/test-ear $modules_dir
cp $poms/ejb-test-ear.pom $modules_dir/test-ear/pom.xml

( 
  cd $modules_dir/test-ear
  mkdir -p src/main/resources
  mv META-INF src/main/resources
)

echo "... assembly"
cp -r $src/modules/assembly $modules_dir
cp $poms/ejb-assembly.pom $modules_dir/assembly/pom.xml

# ---------------------------------------------------

echo "Removing CVS directories"
find $dir -name 'CVS*' -exec rm -rf {} \; > /dev/null 2>&1

echo "Restoring IntelliJ Files"
cp -r intellij/openejb/* openejb

