/******************************************************************
* Author: Tim Urberg - tim_urberg@yahoo.com 
* Description: Contains JavaScript functions used by the webadmin 
* Date: 11/23/2002
*******************************************************************/

function checkDeploy(form)
{
    //set up local variables
    var goodForm = true;
    var msg = "";
   
    if(form.jarFile.value == "")
    {
       msg = "Please enter the full path to your jar file.";
       goodForm = false;
    }
   
    //check for form submission
    if(!goodForm)
    {
        alert(msg);
        return false;
    }
    
    return true;
}