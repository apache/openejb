package org.openejb.test;

/**
 * 
 */
public class ApplicationException extends Exception{

    public ApplicationException(String message){
        super(message);
    }

    public ApplicationException(){
        super();
    }
}
