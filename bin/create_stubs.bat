@echo off
REM   Contributions by:
REM     Pizer Chen  <iceant@21cn.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%/bin/java

REM=== Setup Classpath ===

set CP=
for %%i in (%OPENEJB_HOME%\dist\openejb*.jar) do call cp.bat %%i
for %%i in (%OPENEJB_HOME%\lib\openejb*.jar) do call cp.bat %%i

set CP=%OPENEJB_HOME%\lib\castor-0.9.3.9.jar;%CP%
set CP=%OPENEJB_HOME%\lib\ejb-1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\idb_3.26.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jakarta-regexp-1.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jca_1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jdbc2_0-stdext.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jta_1.0.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\junit_3.5.jar;%CP%
set CP=%OPENEJB_HOME%\lib\log4j-1.2.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xercesImpl-2.0.2.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xmlParserAPIs-2.0.2.jar;%CP%

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

