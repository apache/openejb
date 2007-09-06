Below are instructions on how to build and run the plugin project.

1. Download the xxx_final.zip file attached to this issue
2. Unzip the file
3. open a new eclipse workspace
4. Choose File > Import and then choose Existing projects into workspace
5. Browse to the directory where you unzipped the above zip file.
6. Now check the checkbox for the eclipse plugin project you want to import, and click Finish

Running the plugin
A.
1. open plugin.xml (double-click it)
2. In the Overview tab, click on the link "Launch Eclipse application"
3. This will open a new instance of Eclipse (which will have the above plugin)

B. Adding a new installed runtime
1. Click on Window > Preferences
2. In the Preferences window, navigate to Server > Installed Runtimes
3. On the Installed Runtimes page, click on Add, then choose Apache > OpenEJB 3.0.0
4. Click next and specify the location of the installation directory of OpenEJB
5. Click finish

C. Creating a new server
1. Open the J2EE perspective
2. go to the servers view
3. Right-click anywhere on the servers view and select New > Server (in the context menu)
4. Select OpenEJB from the list

D. Starting and stopping the server
1. Servers view should now have the OpenEJB server instance
2. You can right-click on it and choose start / Stop. (You can also use the toolbar on the servers view to do the same)

DEPLOYING AN EJB
1. Create an EJB project
2. Create an EJB
3. Once you are finished compiling the EJB, drag the EJB project on top of the server in the Servers view
4. Start the server
5. Your EJB is now deployed to the server
