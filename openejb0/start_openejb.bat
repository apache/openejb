REM 
REM This bat starts each of the bat files in separate windows
REM It works on Windows 2000 and NT, I cannot verify that it
REM works on Windows 95/98/ME.  If you have tried it successfully
REM post to the mailing list and let us know so we can remove this
REM header.
REM 
REM David
REM 

REM $Id$

start "OpenORB RMI/IIOP JNDI Naming Server" launch_jndi.bat

start "OpenEJB RMI/IIOP Server" launch_server.bat
