@echo off

set OPENEJB_HOME=o:\dev\openejb

set CP=
for %%i in (%OPENEJB_HOME%\lib\*.jar) do call %OPENEJB_HOME%\bin\cp.bat %%i 
for %%i in (%OPENEJB_HOME%\dist\*.jar) do call %OPENEJB_HOME%\bin\cp.bat %%i 
set CP=%OPENEJB_HOME%\src\examples\conf;%CP%

set CP=%JAVA_HOME%\lib\tools.jar;%CP%

REM %JAVA_HOME%\bin\java -classpath %CP% -Dopenejb.home=%OPENEJB_HOME% com.titan.clients.Client_1
REM %JAVA_HOME%\bin\java -classpath %CP% -Dopenejb.home=%OPENEJB_HOME% com.titan.clients.Client_2
REM %JAVA_HOME%\bin\java -classpath %CP% -Dopenejb.home=%OPENEJB_HOME% com.titan.clients.Client_3
REM %JAVA_HOME%\bin\java -classpath %CP% -Dopenejb.home=%OPENEJB_HOME% com.titan.clients.Client_4
REM %JAVA_HOME%\bin\java -classpath %CP% -Dopenejb.home=%OPENEJB_HOME% com.titan.clients.Client_cleanup
%JAVA_HOME%\bin\java -classpath %CP% -Dopenejb.home=%OPENEJB_HOME% org.acme.clients.HelloWorld

