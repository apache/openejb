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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.core.stateful;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.openejb.core.EnvProps;
import org.openejb.util.FileUtils;
/**
 * 
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class SimplePassivater implements PassivationStrategy {
    private File sessionDirectory;
    final static protected org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance("OpenEJB");

    public void init(Properties props) throws org.openejb.SystemException{
        if (props != null) {
            props = new Properties();
        }

        String dir = props.getProperty(EnvProps.IM_PASSIVATOR_PATH_PREFIX);
        
        try{
            
            if(dir!=null) {
                sessionDirectory = FileUtils.getBase().getDirectory(dir);
            }else {
                sessionDirectory =  new File("java.io.tmpdir");
            }
            logger.info("Using directory "+sessionDirectory+" for stateful session passivation");
        }catch(java.io.IOException e) {
            throw new org.openejb.SystemException(getClass().getName()+".init(): can't use directory prefix "+dir+":"+e);
        }
    }
    
    public void passivate(Object primaryKey, Object state)
    throws org.openejb.SystemException{
        try{
           // The replace(':','=') ensures the filename is correct under Microsoft Windows OS
            String filename = primaryKey.toString().replace(':', '=' );

            File sessionFile = new File( sessionDirectory, filename);

            logger.info("Passivating to file "+sessionFile);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(sessionFile));
            
            oos.writeObject(state);// passivate just the bean instance
            oos.close();
            sessionFile.deleteOnExit();
    }
	catch(java.io.NotSerializableException nse ) {
            logger.info("Passivation failed ", nse);
            throw new org.openejb.SystemException("The type " + nse.getMessage() + " in the bean class " + ((BeanEntry)state).bean.getClass().getName() + " is not serializable as mandated by the EJB specification."); 
	}
	catch(Exception t){
            logger.info("Passivation failed ", t);
            // FIXME: More intelligent exception handling needed
            throw new org.openejb.SystemException(t);
        }
        
    }
    public void passivate(Hashtable hash)throws org.openejb.SystemException{
        Enumeration enum = hash.keys();
        while(enum.hasMoreElements()){
            Object id = enum.nextElement();
            passivate(id, hash.get(id));
        }
    }
    
    /**
     * 
     * @param primaryKey
     * @return 
     * @exception org.openejb.SystemException
     *                   If there is an problem retreiving the instance from the .ser file.
     */
    public Object activate(Object primaryKey) throws org.openejb.SystemException{
        
        try{
            // The replace(':','=') ensures the filename is correct under Microsoft Windows OS
            String filename = primaryKey.toString().replace(':', '=' );
            
            File sessionFile = new File( sessionDirectory, filename);

            if(sessionFile.exists()){                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
                logger.info("Activating from file "+sessionFile);

                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sessionFile));
                Object state = ois.readObject();
                ois.close();
                sessionFile.delete();
                return state; 
            }else{
                logger.info("Activation failed: file not found "+sessionFile);
                return null;
            }
        
        }catch(Exception t){
            logger.info("Activation failed ", t);
            // FIXME: More intelligent exception handling needed
            throw new org.openejb.SystemException(t);
        }
        
    }
    
}
