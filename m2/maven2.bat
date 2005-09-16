@echo off

set root=%CD%\openejb
set poms=%CD%
set modules=%CD%\..\modules
echo \CVS\ > excludes.txt

echo "Removing old version"
if exist %root% rmdir /s /q %root%

echo "Setting up base"
mkdir %root%
copy %poms%\ejb-group.pom %root%\pom.xml


echo "Setting up core..."
set m1Dir=%modules%\core
set m2Dir=%root%\openejb-core
mkdir %m2dir%
copy %poms%\ejb-core.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\bin %m2Dir%\src\main\bin
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\conf %m2Dir%\src\main\conf
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\etc %m2Dir%\src\main\etc
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test %m2Dir%\src\test\java
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test-resources %m2Dir%\src\test\resources
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test-ejb-jar %m2Dir%\src\test\test-ejb-jar
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\java %m2Dir%\src\main\java



echo "Setting up openejb-builder..."
set m1Dir=%modules%\openejb-builder
set m2Dir=%root%\openejb-builder
mkdir  %m2Dir%
copy %poms%\ejb-builder.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test %m2Dir%\src\test\java
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\schema %m2Dir%\src\main\schema
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test-cmp %m2Dir%\src\test\test-cmp
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test-ear %m2Dir%\src\test\test-ear
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test-ejb-jar %m2Dir%\src\test\test-ejb-jar
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\test-resources %m2Dir%\src\test\resources
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\java %m2Dir%\src\main\java



echo "Setting up pkgen-builder..."
set m1Dir=%modules%\pkgen-builder
set m2Dir=%root%\pkgen-builder
mkdir  %m2Dir%
copy %poms%\pkgen-builder.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\java %m2Dir%\src\main\java
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\schema %m2Dir%\src\main\schema



echo "Setting up webadmin..."
set m1Dir=%modules%\webadmin
set m2Dir=%root%\webadmin
mkdir  %m2Dir%
copy %poms%\webadmin.pom %m2Dir%\pom.xml

  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\java %m2Dir%\src\main\java
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\etc %m2Dir%\src\main\etc
  xcopy /EXCLUDE:excludes.txt /E /Q /I %m1Dir%\src\htdocs %m2Dir%\src\main\htdocs


del excludes.txt
set root=
set poms=
set modules=
set m1Dir=
set m2Dir=