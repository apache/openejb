/******************************************************************
* Author: Tim Urberg - tim_urberg@yahoo.com 
* Description: Contains JavaScript functions used by the webadmin 
* Date: 11/23/2002
*******************************************************************/

//checks to make sure the jar file form field is filled in
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

//checks to make sure the second page of the deploy is filled out
function checkDeployValues(form)
{
	//convience variables
	var formName;
	var formValue;
	
	//loop through all elements of the array
	for(var i=0; i<form.elements.length; i++)
	{
		formName = form.elements[i].name;
		formValue = form.elements[i].value;
		
		if(formName.indexOf("Parameters") == -1)
		{
			if(formValue == "")
			{
				alert("All form variables (except OQL parameters) are required.");
				return false;
			}
		}
	}
	
	return true;
}

//submits a form
function submitForm(form, action)
{
	form.action = action;
	form.submit();
}

//confirms whether or not to submit the form
function confirmSubmitForm(form, action, message)
{
  if(confirm(message))
  {
     submitForm(form, action);
  }
}

//opens up a pop-up help window
function popUpHelp(url)
{
   window.open(url, "helpWindow", "width=640,height=480,resizable,scrollbars");
}