@echo off
REM   Contributions by:
REM     Pizer Chen  <iceant@21cn.com>

set JAVA=%JAVA_HOME%/bin/java

REM=== Setup Classpath ===
set CP=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i
for %%i in (lib\*.zip) do call cp.bat %%i
for %%i in (dist\*.jar) do call cp.bat %%i
for %%i in (dist\*.zip) do call cp.bat %%i
for %%i in (test\lib\*.jar) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%

set OPTIONS=
if "%1"=="" (goto Usage) else goto gen

:gen
if "%2"=="" (goto gen1) else if "%3"=="" (goto usage) else goto gen2

:gen1
    %JAVA% -classpath %cp% org.openorb.rmi.compiler.JavaToIdl -tie -stub -noidl %1 
    goto end

:gen2
    %JAVA% -classpath "%cp%" org.openorb.rmi.compiler.JavaToIdl -tie -stub -noidl %*
    goto end

:Usage
    echo Usage: create_stubs.bat [OPTIONS] [CLASSES] ...
    echo "Create the CORBA stubs and ties for a bean's remote and  
    echo  home interface. 
    echo  Options: 
    echo    -d DIRECTORY       output the generated class into 
    echo                       a specific directory 
    echo  Classes: 
    echo    the full class name of the remote or home interface 
    echo  Run the compiler two times : 
    echo    - one for the remote interface 
    echo    - another one for the home interface 
    echo  Example: 
    echo    create_stubs.bat org.openejb.test.beans.DatabaseHome 
    echo    create_stubs.bat -d test/src org.openejb.test.beans.DatabaseHome  
    echo  The class specified must be in the classpath before 
    echo  running create_stub.bat  
    goto end

:end

