@echo off
REM===============================================
REM  Licensed to the Apache Software Foundation (ASF) under one or more
REM  contributor license agreements.  See the NOTICE file distributed with
REM  this work for additional information regarding copyright ownership.
REM  The ASF licenses this file to You under the Apache License, Version 2.0
REM  (the "License"); you may not use this file except in compliance with
REM  the License.  You may obtain a copy of the License at
REM
REM     http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.
REM===============================================

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
copy %poms%\openejb-root-profiles.xml %root%\profiles.xml
maven -q process-root-pom

echo Setting up core...
set m1Dir=%modules%\core
set m2Dir=%root%\openejb-core
mkdir %m2dir%
copy %poms%\openejb-core.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\bin %m2Dir%\src\main\bin
  REM xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\conf %m2Dir%\src\main\conf
  REM xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\etc %m2Dir%\src\main\etc
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\etc\META-INF %m2Dir%\src\main\resources\META-INF
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
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-ant %m2Dir%\src\test\test-ant
  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\src\test-ear %m2Dir%\src\test\test-ear
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
copy %poms%\test-ear.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\META-INF\geronimo-application.xml %m2Dir%\src\main\resources\META-INF

echo Setting up test-ant-ear...
set m2Dir=%root%\test-ant-ear
set m1Dir=%modules%\openejb-builder\src\test-ant
mkdir  %m2Dir%
copy %poms%\test-ant-ear.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\META-INF\geronimo-application.xml %m2Dir%\src\main\resources\META-INF
  xcopy /EXCLUDE:cvs.exclude /Q /I %m1Dir% %root%\openejb-builder\src\test-ant

echo Setting up test-jar...
set m2Dir=%root%\test-ejb-jar
set m1Dir=%modules%\openejb-builder\src\test-ejb-jar
mkdir  %m2Dir%
copy %poms%\test-ejb-jar.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:cvs.exclude /E /Q /I %m1Dir%\META-INF %m2Dir%\src\main\resources\META-INF
  xcopy /EXCLUDE:cvs.exclude /S /Q /I %m1Dir%\org %m2Dir%\src\main\java\org
  xcopy /EXCLUDE:cvs.exclude /Q /I %m1Dir% %root%\openejb-builder\src\test-ejb-jar

echo Copying edited unit test files...
  xcopy /E /Q /I /Y unit-tests\*.* openejb

del cvs.exclude
del java.exclude
set root=
set poms=
set modules=
set m1Dir=
set m2Dir=
