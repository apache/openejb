/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.alt.config;

import org.openejb.alt.config.sys.*;
import org.openejb.alt.config.rules.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.OpenEJBException;
import org.openejb.util.Messages;
import org.openejb.util.FileUtils;
import org.openejb.util.SafeToolkit;
import java.util.Enumeration;
import java.util.Vector;
import java.io.PrintStream;
import java.io.DataInputStream;
import java.io.File;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EjbValidator {

    static protected Messages _messages = new Messages( "org.openejb.util.resources" );

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public EjbValidator() throws OpenEJBException {
    }

    public static EjbSet validateJar(String jarLocation) throws OpenEJBException{
        EjbJar ejbJar = null;
        try {
            ejbJar = EjbJarUtils.readEjbJar(jarLocation);
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new OpenEJBException(e.getMessage());
        }
        
        try {
            return validateJar( ejbJar, jarLocation);
        } catch ( Exception e ) {
            throw new OpenEJBException(e.getMessage());
        }
    }

    public static EjbSet validateJar(EjbJar ejbJar, String jarLocation) throws OpenEJBException{
        try {
            // Create the EjbSet
            EjbSet set = new EjbSet(ejbJar, jarLocation);
            
            // Run the validation rules
            ValidationRule[] rules = getValidationRules();
            for (int i=0; i < rules.length; i++){
                rules[i].validate( set );
            }
            // Report the failures
            return set;
        } catch ( Exception e ) {
            // TODO: Better exception handling.
            e.printStackTrace();
            throw new OpenEJBException(e.getMessage());
        }
    }

    protected static ValidationRule[] getValidationRules(){
        ValidationRule[] rules = new ValidationRule[]{
            new CheckClasses(),
            new CheckMethods()
        };
        return rules;
    }
    
    //Validate all classes are present
    //Validate classes are the correct type
    //Validate ejb references
    //Validate resource references
    //Validate security references

    
    /*------------------------------------------------------*/
    /*    Static methods                                    */
    /*------------------------------------------------------*/

    public static void main(String args[]) {
        try{
            org.openejb.util.ClasspathUtils.addJarsToPath("lib");
            org.openejb.util.ClasspathUtils.addJarsToPath("dist");
        } catch (Exception e){
            e.printStackTrace();
        }

        for (int i=0; i < args.length; i++){
            try{
                EjbSet set = validateJar( args[i] );
                printResults( set );                
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void printResults(EjbSet set){
        System.out.println("------------------------------------------");
        System.out.println(" "+ set.getJarPath() );
        System.out.println("------------------------------------------");
        
        ValidationFailure[] failures = set.getFailures();
        for (int i=0; i < failures.length; i++){
            System.out.print((i+1)+". ");
            System.out.print(failures[i].getBean().getEjbName());
            System.out.print(": ");
            System.out.println(failures[i].getMessage());
            System.out.println();
            System.out.print('\t');
            System.out.println(failures[i].getDetails());
            System.out.println();
        }
        if (failures.length > 0) {
            System.out.println(" "+failures.length+" failures");
        }
        System.out.println();
        System.out.println();
        
        ValidationWarning[] warnings = set.getWarnings();
        for (int i=0; i < warnings.length; i++){
            System.out.print((i+1)+". ");
            System.out.print(warnings[i].getBean().getEjbName());
            System.out.print(": ");
            System.out.println(warnings[i].getMessage());
            System.out.println();
            System.out.print('\t');
            System.out.println(warnings[i].getDetails());
            System.out.println();
        }
        if (warnings.length > 0) {
            System.out.println(" "+warnings.length+" warnings");
        }
    }
}
