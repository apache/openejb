#!/bin/sh

src=`pwd`/..
dir=`pwd`/maven2
poms=`pwd`

rm -rf $dir > /dev/null 2>&1

mkdir -p $dir > /dev/null 2>&1

cp $poms/ejb-group.pom $dir/pom.xml

cp -r $src/modules/core $dir

mv $dir/core/src/test-ejb-jar $dir

# ---------------------------------------------------
# 
# ---------------------------------------------------

(
  cd $dir
  m2 pom:install
)

# ---------------------------------------------------
# 
# ---------------------------------------------------

cp $poms/ejb-test-jar.pom $dir/test-ejb-jar/pom.xml

( 
  cd $dir/test-ejb-jar  
  mkdir -p src/main/java
  mv org src/main/java
  mkdir -p src/main/resources
  mv META-INF src/main/resources
  m2 pom:install
)  

# ---------------------------------------------------

find $dir -name 'CVS*' -exec rm -rf {} \; > /dev/null 2>&1

