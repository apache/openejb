@echo off

set root=%CD%\openejb
set poms=%CD%
set modules=%CD%\..\modules
echo \CVS\ > cvs.exclude
echo *.java > java.exclude

echo Removing old version
if exist %root% rmdir /s /q %root%

echo Setting up base
mkdir %root%
copy %poms%\openejb-root.pom %root%\pom.xml


echo Setting up core...
set m1Dir=%modules%\core
set m2Dir=%root%\openejb-core
mkdir %m2dir%
copy %poms%\openejb-core.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\bin %m2Dir%\src\main\bin
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\conf %m2Dir%\src\main\conf
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\etc %m2Dir%\src\main\etc
  xcopy /EXCLUDE:cvs.exclude+java.exclude /Q /I %m1Dir%\src\java %m2Dir%\src\main\resources
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test %m2Dir%\src\test\java
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-resources %m2Dir%\src\test\resources
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-ejb-jar %m2Dir%\src\test\test-ejb-jar
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\java\*.java %m2Dir%\src\main\java



echo Setting up openejb-builder...
set m1Dir=%modules%\openejb-builder
set m2Dir=%root%\openejb-builder
mkdir  %m2Dir%
copy %poms%\openejb-builder.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test %m2Dir%\src\test\java
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-cmp %m2Dir%\src\test\test-cmp
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-ear %m2Dir%\src\test\test-ear
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-ant %m2Dir%\src\test\test-ant
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-ejb-jar %m2Dir%\src\test\test-ejb-jar
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-resources %m2Dir%\src\test\resources
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\schema %m2Dir%\src\main\schema
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\java %m2Dir%\src\main\java

echo Setting up pkgen-builder...
set m1Dir=%modules%\pkgen-builder
set m2Dir=%root%\pkgen-builder
mkdir  %m2Dir%
copy %poms%\pkgen-builder.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\java %m2Dir%\src\main\java
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\schema %m2Dir%\src\main\schema


echo Setting up openejb-webadmin...
set m1Dir=%modules%\webadmin
set m2Dir=%root%\openejb-webadmin
mkdir  %m2Dir%
copy %poms%\openejb-webadmin.pom %m2Dir%\pom.xml


echo Setting up openejb-webadmin-commons...
set m2Dir=%root%\openejb-webadmin\openejb-webadmin-commons
mkdir  %m2Dir%
copy %poms%\openejb-webadmin-commons.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\*.java %m2Dir%\src\main\java\org\openejb\webadmin

echo Setting up openejb-webadmin-clienttools...
set m2Dir=%root%\openejb-webadmin\openejb-webadmin-clienttools
mkdir  %m2Dir%
copy %poms%\openejb-webadmin-clienttools.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\clienttools\*.java %m2Dir%\src\main\java\org\openejb\webadmin\clienttools
  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\clienttools\*.xml %m2Dir%\src\main\resources\META-INF

echo Setting up openejb-webadmin-ejbgen...
set m2Dir=%root%\openejb-webadmin\openejb-webadmin-ejbgen
mkdir  %m2Dir%
copy %poms%\openejb-webadmin-ejbgen.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\ejbgen\*.java %m2Dir%\src\main\java\org\openejb\webadmin\ejbgen
  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\ejbgen\*.xml %m2Dir%\src\main\resources\META-INF

echo Setting up openejb-webadmin-main...
set m2Dir=%root%\openejb-webadmin\openejb-webadmin-main
mkdir  %m2Dir%
copy %poms%\openejb-webadmin-main.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\main\*.java %m2Dir%\src\main\java\org\openejb\webadmin\main
  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\httpd\*.* %m2Dir%\src\main\java\org\openejb\webadmin\httpd
  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\src\java\org\openejb\webadmin\main\*.xml %m2Dir%\src\main\resources\META-INF

echo Setting up test-ear...
set m2Dir=%root%\test-ear
set m1Dir=%modules%\openejb-builder\src\test-ear
mkdir  %m2Dir%
copy %poms%\ejb-test-ear.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /Q /I %m1Dir% %m2Dir%
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\META-INF %m2Dir%\src\main\resources\META-INF

echo Setting up test-ant-ear...
set m2Dir=%root%\test-ant-ear
set m1Dir=%modules%\openejb-builder\src\test-ant
mkdir  %m2Dir%
copy %poms%\test-ant-ear.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /Q /I %m1Dir% %m2Dir%
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\META-INF %m2Dir%\src\main\resources\META-INF

echo Setting up test-jar...
set m2Dir=%root%\test-ejb-jar
set m1Dir=%modules%\openejb-builder\src\test-ejb-jar
mkdir  %m2Dir%
copy %poms%\ejb-test-jar.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /Q /I %m1Dir% %m2Dir%
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\META-INF %m2Dir%\src\main\resources\META-INF
  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\org %m2Dir%\src\main\java\org

echo Copying edited unit test files...
  xcopy /E /Q /I /Y unit-tests\*.* openejb

echo Installing needed jar files into m2 local repository...
call %M2_HOME%\bin\m2 install:install-file -DgroupId=axis -DartifactId=commons-discovery -Dpackaging=jar -Dversion=SNAPSHOT -Dfile=repository\commons-discovery-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=geronimo -DartifactId=geronimo-deployment -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\geronimo-deployment-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=geronimo -DartifactId=geronimo-j2ee -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\geronimo-j2ee-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=geronimo -DartifactId=geronimo-j2ee-builder -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\geronimo-j2ee-builder-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=geronimo -DartifactId=geronimo-kernel -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\geronimo-kernel-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=geronimo -DartifactId=geronimo-service-builder -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\geronimo-service-builder-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=geronimo -DartifactId=geronimo-system -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\geronimo-system-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=geronimo -DartifactId=geronimo-transaction -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\geronimo-transaction-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=tranql -DartifactId=tranql -Dpackaging=jar -Dversion=1.1-SNAPSHOT -Dfile=repository\tranql-1.1-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=org.codehaus.mojo -DartifactId=maven-xmlbeans-plugin -Dpackaging=jar -Dversion=1.0-SNAPSHOT -Dfile=repository\maven-xmlbeans-plugin-1.0-SNAPSHOT.jar
call %M2_HOME%\bin\m2 install:install-file -DgroupId=org.codehaus.mojo -DartifactId=maven-xmlbeans-plugin -Dpackaging=pom -Dversion=1.0-SNAPSHOT -Dfile=repository\maven-xmlbeans-plugin-1.0-SNAPSHOT.pom


del cvs.exclude
del java.exclude
set root=
set poms=
set modules=
set m1Dir=
set m2Dir=
